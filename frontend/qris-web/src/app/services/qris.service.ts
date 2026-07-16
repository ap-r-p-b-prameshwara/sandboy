import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QrisService {
  private token: string;
  private isSandbox: boolean;

  constructor(private http: HttpClient) {
    const params = new URLSearchParams(window.location.search);
    this.token = params.get('token') || '';
    this.isSandbox = window.location.port === '4204';
    if (this.token) {
      localStorage.setItem('token', this.token);
    }
  }

  private get baseUrl(): string {
    return this.isSandbox ? 'http://localhost:8085' : 'http://localhost:8080';
  }

  private get headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: 'Bearer ' + this.token });
  }

  getTransactions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/qris/transactions`, { headers: this.headers });
  }

  getTransaction(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/api/qris/transactions/${id}`, { headers: this.headers });
  }
}
