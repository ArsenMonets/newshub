import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthResponseDTO, UserDTO } from '../models/models';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthAPI {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/auth';
  
  private readonly userSignal = signal<UserDTO | null>(this.getStoredUser());
  user = this.userSignal.asReadonly();

  register(data: any) {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/register`, data).pipe(
      tap((res: AuthResponseDTO) => this.handleAuth(res))
    );
  }

  login(credentials: any) {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/login`, credentials).pipe(
      tap((res: AuthResponseDTO) => this.handleAuth(res))
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.userSignal.set(null);
  }

  private handleAuth(res: AuthResponseDTO) {
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify(res.user));
    this.userSignal.set(res.user);
  }

  private getStoredUser(): UserDTO | null {
    const userJson = localStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
  }
}