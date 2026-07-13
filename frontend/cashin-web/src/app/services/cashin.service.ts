import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CashinService {
  private apiUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getVirtualAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/cashin/va`);
  }

  getVirtualAccount(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/cashin/va/${id}`);
  }
}

