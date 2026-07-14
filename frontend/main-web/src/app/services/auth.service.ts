import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly PROD_AUTH_URL = 'http://localhost:8080';
  private readonly SANDBOX_AUTH_URL = 'http://localhost:8085';

  constructor(private http: HttpClient, private envService: EnvironmentService) {}

  login(username: string, password: string): Observable<{token: string}> {
    return this.http.post<{token: string}>(`${this.PROD_AUTH_URL}/auth/login`, { username, password }).pipe(
      tap(response => {
        this.setToken(response.token);
        this.setEmail(username);
      })
    );
  }

  register(username: string, password: string, email: string): Observable<any> {
    return this.http.post(`${this.PROD_AUTH_URL}/auth/register`, { username, password, email });
  }

  requestSandboxToken(): Observable<{token: string}> {
    const email = this.getEmail();
    return this.http.post<{token: string}>(`${this.SANDBOX_AUTH_URL}/auth/login`, { email }).pipe(
      tap(response => {
        this.setSandboxToken(response.token);
      })
    );
  }

  setToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    if (this.envService.environment === 'production') {
      return localStorage.getItem('token');
    }
    return localStorage.getItem('sandboxToken');
  }

  setSandboxToken(token: string): void {
    localStorage.setItem('sandboxToken', token);
  }

  getSandboxToken(): string | null {
    return localStorage.getItem('sandboxToken');
  }

  setEmail(email: string): void {
    localStorage.setItem('userEmail', email);
  }

  getEmail(): string | null {
    return localStorage.getItem('userEmail');
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('sandboxToken');
    localStorage.removeItem('userEmail');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }
}

