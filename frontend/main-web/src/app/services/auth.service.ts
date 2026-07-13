import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvironmentService } from './environment.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Auth endpoints ALWAYS go to Production
  private readonly PROD_AUTH_URL = 'http://localhost:8080';

  constructor(private http: HttpClient, private envService: EnvironmentService) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.PROD_AUTH_URL}/auth/login`, { username, password });
  }

  register(username: string, password: string, email: string): Observable<any> {
    return this.http.post(`${this.PROD_AUTH_URL}/auth/register`, { username, password, email });
  }

  setToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}

