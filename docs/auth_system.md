# Мобильная и backend-архитектура авторизации

## Архитектура
- **Протокол**: REST + JSON, короткоживущий access token (JWT, 15 мин) + долгоживущий refresh token (JWT/opaque, 14–30 дней).
- **Базовый путь API**: `http://127.0.0.1:8000/api/v1/` с auth-роутами `/auth/signup`, `/auth/login`, `/auth/refresh`, `/auth/logout` и защищённым профилем `GET /me`.
- **Хранение паролей**: Argon2id (предпочтительно) или bcrypt с уникальной солью и параметрами по безопасности платформы.
- **Хранение токенов**: на клиенте — `EncryptedSharedPreferences` (Android Keystore) или Keychain; refresh хранится отдельно и читается только в фоне при обновлении.
- **Валидация**: middleware на backend проверяет подпись access token, срок и audience; при 401 клиент пытается одноразово обновить токен и повторяет запрос.
- **Logout**: очистка секретного хранилища и (опционально) отзыв refresh токена в БД/блеклисте.

## Клиент (Android, Compose + OkHttp/Retrofit)
### Сетевые сущности
```kotlin
// data/auth/AuthApi.kt
import com.squareup.moshi.Json

interface AuthApi {
    @POST("auth/signup")
    suspend fun signup(@Body payload: SignupRequest): AuthTokens

    @POST("auth/login")
    suspend fun login(@Body payload: LoginRequest): AuthTokens

    @POST("auth/refresh")
    suspend fun refresh(@Body payload: RefreshRequest): AuthTokens

    @POST("auth/logout")
    suspend fun logout(@Body payload: LogoutRequest)

    @GET("me")
    suspend fun me(): MeProfile
}

data class SignupRequest(val email: String, val password: String, val name: String)
data class LoginRequest(val email: String, val password: String)
data class RefreshRequest(@Json(name = "refresh_token") val refreshToken: String)
data class LogoutRequest(@Json(name = "refresh_token") val refreshToken: String)

data class AuthTokens(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_in") val expiresInSeconds: Long
)
```

### Безопасное хранилище токенов
```kotlin
// data/auth/TokenStorage.kt
class TokenStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(tokens: AuthTokens) {
        prefs.edit().apply {
            putString("access", tokens.accessToken)
            putString("refresh", tokens.refreshToken)
            putLong("expires_at", System.currentTimeMillis() + tokens.expiresInSeconds * 1000)
        }.apply()
    }

    fun getAccess(): String? = prefs.getString("access", null)
    fun getRefresh(): String? = prefs.getString("refresh", null)
    fun isAccessExpired(): Boolean =
        System.currentTimeMillis() >= prefs.getLong("expires_at", 0L)

    fun clear() { prefs.edit().clear().apply() }
}
```

### Репозиторий авторизации
  ```kotlin
  // data/auth/AuthRepository.kt
  class AuthRepository(
      private val api: AuthApi,
      private val storage: TokenStorage
  ) {
      suspend fun signup(email: String, password: String, name: String) {
          val tokens = api.signup(SignupRequest(email, password, name))
          storage.save(tokens)
      }

      suspend fun login(email: String, password: String) {
          val tokens = api.login(LoginRequest(email, password))
          storage.save(tokens)
      }

      suspend fun refresh(): Boolean {
          val refresh = storage.getRefresh() ?: return false
          return try {
              val tokens = api.refresh(RefreshRequest(refresh))
              storage.save(tokens)
              true
          } catch (_: HttpException) {
              storage.clear()
              false
          }
      }

      suspend fun logout() {
          storage.getRefresh()?.let { api.logout(LogoutRequest(it)) }
          storage.clear()
      }
  }
  ```

### Interceptor для автоматического обновления
```kotlin
// network/AuthInterceptor.kt
class AuthInterceptor(
    private val storage: TokenStorage,
    private val repository: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        storage.getAccess()?.let { token ->
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)
        if (response.code == 401 && repository.refresh()) {
            response.close()
            val retried = request.newBuilder()
                .header("Authorization", "Bearer ${storage.getAccess()}")
                .build()
            return chain.proceed(retried)
        }
        return response
    }
}
```

### ViewModel-поток для UI
```kotlin
// ui/auth/AuthViewModel.kt
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val state: StateFlow<AuthState> = _state

    fun signup(email: String, password: String, name: String) = viewModelScope.launch {
        runCatching { repository.signup(email, password, name) }
            .onSuccess { _state.value = AuthState.LoggedIn }
            .onFailure { _state.value = AuthState.Error(it.message ?: "Unknown") }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        runCatching { repository.login(email, password) }
            .onSuccess { _state.value = AuthState.LoggedIn }
            .onFailure { _state.value = AuthState.Error(it.message ?: "Unknown") }
    }

    fun logout() { repository.logout(); _state.value = AuthState.LoggedOut }
}

sealed interface AuthState { object LoggedOut : AuthState; object LoggedIn : AuthState; data class Error(val message: String) : AuthState }
```

### Обработка ошибок/сети
- Любой запрос заворачивается в `runCatching {}`; при `UnknownHostException` показываем баннер оффлайна.
- При `401` перезапрос выполняется только один раз, иначе — выход/экран логина.
- Перед выполнением запроса можно проверять `TokenStorage.isAccessExpired()` и сразу инициировать refresh.

## Backend (Ktor + JWT + Argon2)
### Конфигурация безопасности
```kotlin
fun Application.security(jwtSecret: String) {
    install(Authentication) {
        jwt {
            verifier(JWT
                .require(Algorithm.HMAC512(jwtSecret))
                .withIssuer("onboarding-app")
                .build())
            validate { credential ->
                if (credential.payload.expiresAt.time > System.currentTimeMillis()) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
```

### Модель пользователя и репозиторий
```kotlin
data class User(val id: UUID, val email: String, val name: String, val passwordHash: String)

class UserRepository(private val db: Database) {
    suspend fun findByEmail(email: String): User? = db.users.find { it.email == email }
    suspend fun insert(user: User) { db.users += user }
}
```

### Хеширование пароля (Argon2id)
```kotlin
object Passwords {
    private val hasher = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)

    fun hash(raw: String): String = hasher.hash(2, 65536, 1, raw.toCharArray())
    fun verify(raw: String, hash: String): Boolean = hasher.verify(hash, raw.toCharArray())
}
```

### Генерация токенов
```kotlin
fun generateTokens(userId: UUID, secret: String): AuthTokensDto {
    val access = JWT.create()
        .withIssuer("onboarding-app")
        .withSubject(userId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000))
        .sign(Algorithm.HMAC512(secret))

    val refreshId = UUID.randomUUID().toString()
    val refresh = JWT.create()
        .withIssuer("onboarding-app")
        .withClaim("type", "refresh")
        .withSubject(userId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000))
        .withJWTId(refreshId)
        .sign(Algorithm.HMAC512(secret))

    // refreshId может сохраняться в таблицу refresh_token для отзыва
    return AuthTokensDto(access, refresh, 15 * 60)
}

data class AuthTokensDto(val accessToken: String, val refreshToken: String, val expiresInSeconds: Long)
```

### Роуты signup/login/refresh
```kotlin
fun Route.authRoutes(repo: UserRepository, secret: String) {
    post("/auth/signup") {
        val req = call.receive<SignupRequest>()
        require(req.password.length >= 8) { "Password too short" }
        if (repo.findByEmail(req.email) != null) return@post call.respond(HttpStatusCode.Conflict)

        val user = User(UUID.randomUUID(), req.email, req.name, Passwords.hash(req.password))
        repo.insert(user)
        call.respond(generateTokens(user.id, secret))
    }

    post("/auth/login") {
        val req = call.receive<LoginRequest>()
        val user = repo.findByEmail(req.email) ?: return@post call.respond(HttpStatusCode.Unauthorized)
        if (!Passwords.verify(req.password, user.passwordHash)) return@post call.respond(HttpStatusCode.Unauthorized)
        call.respond(generateTokens(user.id, secret))
    }

    authenticate {
        post("/auth/refresh") {
            val req = call.receive<RefreshRequest>()
            val decoded = JWT.require(Algorithm.HMAC512(secret)).build().verify(req.refreshToken)
            if (decoded.getClaim("type").asString() != "refresh") return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(generateTokens(UUID.fromString(decoded.subject), secret))
        }
    }
}
```

### Middleware/guards
- `Authentication` плагин Ktor защищает приватные роуты.
- При logout можно помечать `jti` refresh-токена как отозванный и проверять его в таблице/кеше перед выдачей новых токенов.

## Пояснения и выборы
- **JWT + refresh**: минимизируем количество логинов и держим access токен коротким. JWT позволяет stateless-валидацию на backend, а refresh при необходимости можно отзывать в БД.
- **Argon2id/bcrypt**: современные алгоритмы с защитой от перебора; параметры (memory/time cost) можно калибровать под инфраструктуру.
- **EncryptedSharedPreferences**: использует Android Keystore, не требует root и не уязвима для простого дампа, безопаснее, чем plain SharedPreferences или localStorage.
- **Interceptor**: централизует добавление токена и повтор запроса после refresh, исключает дублирование логики по экранам.
- **Четкие ошибки**: различаем 401 (нужен refresh/логин), 409 (почта занята), сетевые ошибки — для UX и аналитики.
