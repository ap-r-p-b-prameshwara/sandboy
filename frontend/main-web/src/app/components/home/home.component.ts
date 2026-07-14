import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
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
        <p *ngIf="loading" class="loading">Loading...</p>
      </div>

      <div class="main-layout">
        <div class="sidebar">
          <ul>
            <li>Dashboard</li>
            <li *ngIf="hasQrisPrivilege">QRIS</li>
            <li>Cash In</li>
          </ul>
        </div>
        <div class="content">
          <p>Email: {{ authService.getEmail() }}</p>
          <p>Environment: {{ selectedEnv | uppercase }}</p>

          <div *ngIf="!hasQrisPrivilege" class="activation-card">
            <h3>Aktivasi Merchant QRIS</h3>
            <p>Aktifkan QRIS untuk menerima pembayaran melalui QR Code.</p>
            <button (click)="activateQris()" [disabled]="activating">
              {{ activating ? 'Mengaktifkan...' : 'Aktivasi Sekarang' }}
            </button>
            <p *ngIf="activationError" class="error">{{ activationError }}</p>
          </div>
        </div>
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
    .main-layout { display: flex; gap: 20px; margin-top: 20px; }
    .sidebar { width: 200px; background: #f5f5f5; padding: 15px; border-radius: 8px; }
    .sidebar ul { list-style: none; padding: 0; }
    .sidebar li { padding: 10px; cursor: pointer; border-radius: 4px; }
    .sidebar li:hover { background: #e0e0e0; }
    .content { flex: 1; }
    .activation-card {
      background: #fff3cd; border: 1px solid #ffc107;
      padding: 20px; border-radius: 8px; max-width: 400px;
    }
    .activation-card button {
      background: #007bff; color: white; border: none;
      padding: 10px 20px; border-radius: 4px; cursor: pointer;
    }
    .activation-card button:disabled { background: #ccc; }
    .error { color: red; font-size: 12px; }
  `]
})
export class HomeComponent {
  selectedEnv: 'production' | 'sandbox' = 'production';
  loading = false;
  activating = false;
  activationError = '';
  hasQrisPrivilege = false;

  constructor(
    public authService: AuthService,
    private envService: EnvironmentService,
    private http: HttpClient
  ) {
    this.selectedEnv = this.envService.environment;
    this.checkPrivileges();
  }

  checkPrivileges(): void {
    const url = this.envService.apiUrl + '/api/privileges';
    const token = this.authService.getToken();
    if (!token) return;
    this.http.get<any>(url, { headers: { Authorization: 'Bearer ' + token } })
      .subscribe({
        next: (res) => {
          if (res && res.privileges) {
            this.hasQrisPrivilege = res.privileges.includes('QRIS');
          }
        },
        error: () => {}
      });
  }

  activateQris(): void {
    this.activating = true;
    this.activationError = '';
    const url = this.envService.apiUrl + '/api/qris/activate';
    const token = this.authService.getToken();
    this.http.post(url, {
      merchantName: this.authService.getEmail(),
      nmid: this.selectedEnv + Date.now(),
      dailyLimit: 10000000
    }, { headers: { Authorization: 'Bearer ' + token } })
      .subscribe({
        next: () => {
          this.activating = false;
          this.checkPrivileges();
        },
        error: (err) => {
          this.activating = false;
          this.activationError = 'Gagal aktivasi: ' + (err.error?.message || err.message);
        }
      });
  }

  onEnvChange(): void {
    this.envService.setEnvironment(this.selectedEnv);
    if (this.selectedEnv === 'sandbox' && !this.authService.getSandboxToken()) {
      this.loading = true;
      this.authService.requestSandboxToken().subscribe({
        next: () => {
          this.loading = false;
          this.checkPrivileges();
        },
        error: () => this.loading = false
      });
    } else {
      this.checkPrivileges();
    }
  }

  onLogout(): void {
    this.authService.logout();
    window.location.reload();
  }
}
