import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private _environment: 'production' | 'sandbox' = 'production';

  setEnvironment(env: 'production' | 'sandbox'): void {
    this._environment = env;
  }

  get environment(): 'production' | 'sandbox' {
    return this._environment;
  }

  get apiUrl(): string {
    return this._environment === 'production' 
      ? 'http://localhost:8080' 
      : 'http://localhost:8085';
  }
}

