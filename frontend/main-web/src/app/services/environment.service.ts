import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {
  private _environment: 'production' | 'sandbox' = 'production';

  constructor() {
    const saved = localStorage.getItem('appEnvironment');
    if (saved === 'production' || saved === 'sandbox') {
      this._environment = saved;
    }
  }

  setEnvironment(env: 'production' | 'sandbox'): void {
    this._environment = env;
    localStorage.setItem('appEnvironment', env);
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

