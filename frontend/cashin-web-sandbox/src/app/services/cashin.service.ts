import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CashinService {
  private token: string;

  constructor(private http: HttpClient) {
    const params = new URLSearchParams(window.location.search);
    this.token = params.get('token') || '';
    if (this.token) {
      localStorage.setItem('sandboxToken', this.token);
    }
  }

  private get baseUrl(): string {
    return 'http://localhost:8085';
  }

  private get headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: 'Bearer ' + this.token });
  }

  getVirtualAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/cashin/va`, { headers: this.headers });
  }

  getVirtualAccount(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/api/cashin/va/${id}`, { headers: this.headers });
  }
}
