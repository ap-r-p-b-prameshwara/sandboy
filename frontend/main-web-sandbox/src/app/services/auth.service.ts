import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly SANDBOX_AUTH_URL = 'http://localhost:8085';

  constructor(private http: HttpClient) {}

  login(email: string): Observable<{token: string}> {
    return this.http.post<{token: string}>(`${this.SANDBOX_AUTH_URL}/api/login`, { email }).pipe(
      tap(response => {
        this.setSandboxToken(response.token);
        this.setEmail(email);
      })
    );
  }

  getToken(): string | null {
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
    localStorage.removeItem('sandboxToken');
    localStorage.removeItem('userEmail');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('sandboxToken');
  }
}

