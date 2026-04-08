export interface User {
  id: string;
  email: string;
  displayName: string;
  role: 'admin' | 'user';
  devices: string[];
  createdAt: string;
  updatedAt: string;
}

export interface UserCredentials {
  email: string;
  password: string;
}

export interface UserRegistration extends UserCredentials {
  displayName: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface AuthResponse {
  user: Omit<User, 'devices'>;
  tokens: AuthTokens;
}
