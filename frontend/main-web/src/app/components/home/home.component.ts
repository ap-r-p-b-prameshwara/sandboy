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
          <h1>Sandbox Dashboard</h1>
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
        <p *ngIf="loading" class="loading">Mendapatkan token sandbox...</p>
      </div>
      <div class="content">
        <p *ngIf="!loading">Anda login sebagai: {{ authService.getEmail() }}</p>
        <p *ngIf="!loading">Environment aktif: {{ selectedEnv | uppercase }}</p>
      </div>
    </div>
  `,
  styles: [`
    .home-container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; }
    .brand { display: flex; align-items: center; }
    .logo { width: 70px; height: auto; display: inline-block; margin-right: 12px; }
    .brand h1 { margin: 0; }
    .env-switcher { margin: 20px 0; padding: 15px; background: #f5f5f5; border-radius: 8px; }
    .loading { color: #666; font-style: italic; }
    select { padding: 8px; margin-left: 10px; border-radius: 4px; border: 1px solid #ccc; }
    .content { margin-top: 20px; }
  `]
})
export class HomeComponent {
  selectedEnv: 'production' | 'sandbox' = 'production';
  loading = false;

  constructor(
    public authService: AuthService,
    private envService: EnvironmentService
  ) {
    this.selectedEnv = this.envService.environment;
  }

  get apiUrl(): string {
    return this.envService.apiUrl;
  }

  onEnvChange(): void {
    this.envService.setEnvironment(this.selectedEnv);

    if (this.selectedEnv === 'sandbox' && !this.authService.getSandboxToken()) {
      this.loading = true;
      this.authService.requestSandboxToken().subscribe({
        next: () => this.loading = false,
        error: () => {
          this.loading = false;
          console.error('Gagal mendapatkan token sandbox');
        }
      });
    }
  }

  onLogout(): void {
    this.authService.logout();
    window.location.reload();
  }
}

