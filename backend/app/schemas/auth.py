from pydantic import BaseModel, EmailStr, Field


class TokenResponse(BaseModel):
    accessToken: str
    refreshToken: str
    expiresInSeconds: int


class SignupRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8)
    name: str = Field(min_length=1, max_length=200)


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=1)


class RefreshRequest(BaseModel):
    refreshToken: str = Field(min_length=1)


class LogoutRequest(BaseModel):
    refreshToken: str = Field(min_length=1)
