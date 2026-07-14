import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  get environment(): 'production' | 'sandbox' {
    return 'sandbox';
  }

  get apiUrl(): string {
    return 'http://localhost:8085';
  }
}

