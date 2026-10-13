/** Mirrors `LoginRequestDTO`. */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Mirrors `RegisterRequestDTO`. */
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

/** Mirrors `TokenResponseDTO`. `expiresIn` is the token lifetime in milliseconds. */
export interface TokenResponse {
  token: string;
  expiresIn: number;
}
