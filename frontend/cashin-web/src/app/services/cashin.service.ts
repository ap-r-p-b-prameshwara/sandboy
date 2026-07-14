import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CashinService {
  constructor(private http: HttpClient) {}

  private get baseUrl(): string {
    const env = localStorage.getItem('appEnvironment') || 'production';
    return env === 'sandbox' ? 'http://localhost:8085' : 'http://localhost:8080';
  }

  getVirtualAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/cashin/va`);
  }

  getVirtualAccount(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/api/cashin/va/${id}`);
  }
}

