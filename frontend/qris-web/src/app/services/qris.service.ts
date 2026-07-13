import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QrisService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getTransactions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/qris/transactions`);
  }

  getTransaction(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/qris/transactions/${id}`);
  }
}

