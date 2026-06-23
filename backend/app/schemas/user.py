from pydantic import BaseModel, EmailStr


class AuthenticatedUser(BaseModel):
    user_id: str


class UserResponse(BaseModel):
    id: str
    email: EmailStr
    name: str
