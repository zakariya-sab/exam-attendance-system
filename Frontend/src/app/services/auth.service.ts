import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { LoginRequest, LoginResponse } from '../models/auth.model';

const API_BASE_URL = 'http://localhost:8080';
const TOKEN_KEY = 'authToken';

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);

  login(username: string, password: string): Observable<LoginResponse> {
    const body: LoginRequest = { username, password };
    return this.http.post<LoginResponse>(`${API_BASE_URL}/api/auth/login`, body);
  }

  saveToken(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }
}
