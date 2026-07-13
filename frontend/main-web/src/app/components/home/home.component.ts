import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { EnvironmentService } from '../../services/environment.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="home-container">
      <div class="header">
        <div class="brand">
          <img src="assets/sandboy.png" alt="Sandboy Logo" class="logo">
          <h1>Welcome to Sandbox</h1>
        </div>
        <button (click)="onLogout()">Logout</button>
      </div>
      <div class="env-switcher">
        <label>Environment:</label>
        <select [(ngModel)]="selectedEnv" (change)="onEnvChange()">
          <option value="production">Production</option>
          <option value="sandbox">Sandbox</option>
        </select>
        <p>API URL: {{ apiUrl }}</p>
      </div>
      <div class="content">
        <p>You are logged in!</p>
      </div>
    </div>
  `,
  styles: [`
    .home-container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; }
    .brand { display: flex; align-items: center; }
    .logo { width: 70px; height: auto; display: inline-block; margin-right: 12px; }
    .brand h1 { margin: 0; }
    .env-switcher { margin: 20px 0; padding: 15px; background: #f5f5f5; }
    select { padding: 8px; margin-left: 10px; }
  `]
})
export class HomeComponent {
  selectedEnv: 'production' | 'sandbox' = 'production';

  constructor(
    private authService: AuthService,
    private envService: EnvironmentService
  ) {
    this.selectedEnv = this.envService.environment;
  }

  get apiUrl(): string {
    return this.envService.apiUrl;
  }

  onEnvChange(): void {
    this.envService.setEnvironment(this.selectedEnv);
  }

  onLogout(): void {
    this.authService.logout();
    window.location.reload();
  }
}

