import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { EnvironmentService } from '../../services/environment.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <img src="assets/sandboy.png" alt="Sandboy Logo" class="toolbar-logo">
      <span class="toolbar-title">Sandbox Dashboard</span>
      <span *ngIf="selectedEnv === 'sandbox'" class="sandbox-badge">SANDBOX</span>
      <span class="spacer"></span>
      <mat-form-field appearance="outline" class="env-select-toolbar">
        <mat-select
          [(ngModel)]="selectedEnv"
          (selectionChange)="onEnvChange()">
          <mat-option value="production">Production</mat-option>
          <mat-option value="sandbox">Sandbox</mat-option>
        </mat-select>
      </mat-form-field>
      <span class="env-status" *ngIf="loading">
        <mat-progress-spinner diameter="20" mode="indeterminate"></mat-progress-spinner>
      </span>
      <button mat-icon-button (click)="onLogout()" matTooltip="Logout" aria-label="Logout">
        <mat-icon>logout</mat-icon>
        <span class="logout-label">Logout</span>
      </button>
    </mat-toolbar>

    <div class="container">
      <mat-card class="env-card">
        <mat-card-content>
          <p class="env-info">
            Environment: <strong>{{ selectedEnv | uppercase }}</strong> |
            API URL: <code>{{ apiUrl }}</code>
          </p>
          <p>Signed in as <strong>{{ authService.getEmail() }}</strong></p>
        </mat-card-content>
      </mat-card>

      <div class="main-layout">
        <mat-card class="sidebar">
          <mat-nav-list>
            <a mat-list-item class="sidebar-item">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Dashboard</span>
            </a>
            <a mat-list-item class="sidebar-item" href="{{ qrisUrl }}" target="_blank" *ngIf="hasQrisPrivilege">
              <mat-icon matListItemIcon>qr_code_2</mat-icon>
              <span matListItemTitle>QRIS ({{ selectedEnv }})</span>
            </a>
            <a mat-list-item class="sidebar-item" href="{{ cashinUrl }}" target="_blank">
              <mat-icon matListItemIcon>account_balance</mat-icon>
              <span matListItemTitle>Cash In ({{ selectedEnv }})</span>
            </a>
          </mat-nav-list>
        </mat-card>

        <div class="content">
          <mat-card *ngIf="!hasQrisPrivilege" class="activation-card">
            <mat-card-header>
              <div mat-card-avatar class="activation-avatar">
                <mat-icon>qr_code</mat-icon>
              </div>
              <mat-card-title>Aktivasi Merchant QRIS</mat-card-title>
              <mat-card-subtitle>Aktifkan QRIS untuk menerima pembayaran</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <p>Aktifkan QRIS untuk menerima pembayaran melalui QR Code.</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="accent" (click)="activateQris()" [disabled]="activating">
                <mat-icon>flash_on</mat-icon>
                {{ activating ? 'Mengaktifkan...' : 'Aktivasi Sekarang' }}
              </button>
            </mat-card-actions>
            <mat-card-footer *ngIf="activationError">
              <p class="activation-error">{{ activationError }}</p>
            </mat-card-footer>
          </mat-card>

          <mat-card *ngIf="hasQrisPrivilege" class="activated-card">
            <mat-card-header>
              <div mat-card-avatar class="activated-avatar">
                <mat-icon>check_circle</mat-icon>
              </div>
              <mat-card-title>QRIS Aktif</mat-card-title>
              <mat-card-subtitle>Anda sudah dapat menerima pembayaran QRIS</mat-card-subtitle>
            </mat-card-header>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .app-toolbar {
      position: sticky;
      top: 0;
      z-index: 10;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
    }
    .toolbar-logo {
      width: 40px;
      height: 40px;
      margin-right: 12px;
      border-radius: 6px;
      background: white;
      object-fit: contain;
    }
    .toolbar-title {
      font-size: 1.15rem;
      font-weight: 500;
    }
    .sandbox-badge {
      background: #ff5722;
      color: white;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 11px;
      margin-left: 8px;
    }
    .env-select-toolbar {
      min-width: 160px;
      margin-right: 8px;
      font-size: 0.85rem;
    }
    .env-select-toolbar ::ng-deep .mdc-notched-outline__leading,
    .env-select-toolbar ::ng-deep .mdc-notched-outline__trailing,
    .env-select-toolbar ::ng-deep .mdc-notched-outline__notch {
      border-color: rgba(255, 255, 255, 0.3) !important;
    }
    .env-select-toolbar ::ng-deep .mat-mdc-select-value-text,
    .env-select-toolbar ::ng-deep .mat-mdc-select-arrow {
      color: white !important;
    }
    .env-select-toolbar ::ng-deep .mat-mdc-select-min-line {
      color: white !important;
    }
    .logout-label {
      margin-left: 6px;
      font-size: 0.9rem;
    }
    .env-card { margin-bottom: 20px; }
    .env-info {
      margin: 8px 0 0;
      color: rgba(0, 0, 0, 0.7);
    }
    .env-status {
      display: inline-flex;
      align-items: center;
      margin-right: 8px;
    }
    .sidebar-item { cursor: default; }
    .activation-card,
    .activated-card {
      max-width: 520px;
    }
    .activation-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #fff3cd;
      color: #ff8f00;
    }
    .activated-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e8f5e9;
      color: #2e7d32;
    }
    .activation-error {
      color: #c62828;
      font-size: 0.85rem;
      margin: 0;
      padding: 8px 16px 16px;
    }
  `]
})
export class HomeComponent {
  selectedEnv: 'production' | 'sandbox' = 'production';
  loading = false;
  activating = false;
  activationError = '';
  hasQrisPrivilege = false;

  get apiUrl(): string {
    return this.envService.apiUrl;
  }

  get qrisUrl(): string {
    return this.envService.environment === 'production'
      ? 'http://localhost:4201'
      : 'http://localhost:4204';
  }

  get cashinUrl(): string {
    return this.envService.environment === 'production'
      ? 'http://localhost:4202'
      : 'http://localhost:4205';
  }

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
            this.hasQrisPrivilege = res.privileges.some((p: any) => p.feature === 'QRIS' && p.enabled);
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
        error: () => {
          this.loading = false;
          this.selectedEnv = 'production';
          this.envService.setEnvironment('production');
        }
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
