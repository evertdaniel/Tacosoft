import { Role } from './domain.types';

export interface ApiError {
  message: string;
  status?: number;
}

export interface LoginRequestBody {
  username: string;
  password: string;
}

export interface LoginSuccessBody {
  token: string;
  username: string;
  role: Role;
}
