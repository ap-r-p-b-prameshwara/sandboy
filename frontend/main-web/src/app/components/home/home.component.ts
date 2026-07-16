import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
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
    CommonModule, FormsModule,
    MatToolbarModule, MatCardModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatListModule,
    MatProgressSpinnerModule, MatDividerModule
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <img src="assets/sandboy.png" class="toolbar-logo">
      <span class="toolbar-title">Sandbox Dashboard</span>
      <span *ngIf="selectedEnv === 'sandbox'" class="sandbox-badge">SANDBOX</span>
      <span class="spacer"></span>
      <mat-form-field appearance="outline" class="env-select-toolbar">
        <mat-select [(ngModel)]="selectedEnv" (selectionChange)="onEnvChange()">
          <mat-option value="production">Production</mat-option>
          <mat-option value="sandbox">Sandbox</mat-option>
        </mat-select>
      </mat-form-field>
      <span class="env-status" *ngIf="loading">
        <mat-progress-spinner diameter="20" mode="indeterminate"></mat-progress-spinner>
      </span>
      <button mat-icon-button (click)="onLogout()" matTooltip="Logout">
        <mat-icon>logout</mat-icon>
      </button>
    </mat-toolbar>

    <div class="container">
      <mat-card class="env-card">
        <mat-card-content>
          <p class="env-info">Signed in as <strong>{{ authService.getEmail() }}</strong></p>
          <p class="env-info">API: <code>{{ apiUrl }}</code></p>
        </mat-card-content>
      </mat-card>

      <div class="main-layout">
        <mat-card class="sidebar">
          <mat-nav-list>
            <a mat-list-item class="sidebar-item" (click)="activeMicro = ''">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Dashboard</span>
            </a>
            <a mat-list-item class="sidebar-item" (click)="openMicro('qris')" *ngIf="hasQrisPrivilege">
              <mat-icon matListItemIcon>qr_code_2</mat-icon>
              <span matListItemTitle>QRIS ({{ selectedEnv }})</span>
            </a>
            <a mat-list-item class="sidebar-item" (click)="openMicro('cashin')">
              <mat-icon matListItemIcon>account_balance</mat-icon>
              <span matListItemTitle>Cash In ({{ selectedEnv }})</span>
            </a>
          </mat-nav-list>
        </mat-card>

        <div class="content">
          <div *ngIf="!activeMicro" class="default-content">
            <mat-card *ngIf="!hasQrisPrivilege" class="activation-card">
              <mat-card-header>
                <div mat-card-avatar class="activation-avatar"><mat-icon>qr_code</mat-icon></div>
                <mat-card-title>Aktivasi Merchant QRIS</mat-card-title>
                <mat-card-subtitle>Daftarkan merchant untuk menerima pembayaran QRIS</mat-card-subtitle>
              </mat-card-header>
              <mat-card-actions>
                <button mat-raised-button color="accent" (click)="activateQris()" [disabled]="activating">
                  {{ activating ? 'Mengaktifkan...' : 'Aktivasi Sekarang' }}
                </button>
              </mat-card-actions>
              <mat-card-footer *ngIf="activationError">
                <p class="activation-error">{{ activationError }}</p>
              </mat-card-footer>
            </mat-card>
            <mat-card *ngIf="hasQrisPrivilege" class="activated-card">
              <mat-card-header>
                <div mat-card-avatar class="activated-avatar"><mat-icon>check_circle</mat-icon></div>
                <mat-card-title>QRIS Aktif</mat-card-title>
                <mat-card-subtitle>Anda sudah dapat menerima pembayaran QRIS</mat-card-subtitle>
              </mat-card-header>
            </mat-card>
          </div>

          <div *ngIf="activeMicro" class="micro-container">
            <button mat-icon-button class="close-btn" (click)="activeMicro = ''"><mat-icon>close</mat-icon></button>
            <iframe [src]="microUrl" class="micro-frame"></iframe>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .app-toolbar { position: sticky; top: 0; z-index: 10; box-shadow: 0 2px 6px rgba(0,0,0,0.18); }
    .toolbar-logo { width: 40px; height: 40px; margin-right: 12px; border-radius: 6px; background: white; object-fit: contain; }
    .sandbox-badge { background: #ff5722; color: white; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-left: 8px; }
    .env-select-toolbar { min-width: 160px; margin-right: 8px; font-size: 0.85rem; }
    .container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .env-card { margin-bottom: 20px; }
    .env-info { margin: 4px 0; color: rgba(0,0,0,0.7); }
    .main-layout { display: flex; gap: 20px; margin-top: 20px; }
    .sidebar { width: 200px; min-width: 200px; }
    .sidebar .sidebar-item { cursor: pointer; }
    .content { flex: 1; }
    .activation-card, .activated-card { max-width: 520px; }
    .activation-avatar, .activated-avatar { display: flex; align-items: center; justify-content: center; }
    .activation-avatar { background: #fff3cd; color: #ff8f00; }
    .activated-avatar { background: #e8f5e9; color: #2e7d32; }
    .activation-error { color: #c62828; font-size: 0.85rem; margin: 0; padding: 8px 16px 16px; }
    .micro-container { position: relative; height: 80vh; }
    .micro-frame { width: 100%; height: 100%; border: none; border-radius: 8px; }
    .close-btn { position: absolute; top: 8px; right: 8px; z-index: 10; background: white; }
  `]
})
export class HomeComponent {
  selectedEnv: 'production' | 'sandbox' = 'production';
  loading = false;
  activating = false;
  activationError = '';
  hasQrisPrivilege = false;
  activeMicro = '';
  microUrl: SafeResourceUrl = '';

  constructor(
    public authService: AuthService,
    private envService: EnvironmentService,
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) {
    this.selectedEnv = this.envService.environment;
    this.checkPrivileges();
  }

  get apiUrl(): string { return this.envService.apiUrl; }

  openMicro(name: string): void {
    const sb = this.selectedEnv === 'sandbox';
    const port = name === 'qris' ? (sb ? '4204' : '4201') : (sb ? '4205' : '4202');
    const token = this.authService.getToken();
    this.activeMicro = name;
    this.microUrl = this.sanitizer.bypassSecurityTrustResourceUrl('http://localhost:' + port + '?token=' + token);
  }

  checkPrivileges(): void {
    const token = this.authService.getToken();
    if (!token) return;
    this.http.get<any>(this.envService.apiUrl + '/api/privileges', {
      headers: { Authorization: 'Bearer ' + token }
    }).subscribe({
      next: (res) => {
        if (res?.privileges) {
          this.hasQrisPrivilege = res.privileges.some((p: any) => p.feature === 'QRIS' && p.enabled);
        }
      }
    });
  }

  activateQris(): void {
    this.activating = true;
    this.activationError = '';
    const token = this.authService.getToken();
    this.http.post(this.envService.apiUrl + '/api/qris/activate', {
      merchantName: this.authService.getEmail(),
      nmid: this.selectedEnv + Date.now(),
      dailyLimit: 10000000
    }, { headers: { Authorization: 'Bearer ' + token } }).subscribe({
      next: () => { this.activating = false; this.checkPrivileges(); },
      error: (err) => {
        this.activating = false;
        this.activationError = 'Gagal: ' + (err.error?.message || err.message);
      }
    });
  }

  onEnvChange(): void {
    this.envService.setEnvironment(this.selectedEnv);
    this.activeMicro = '';
    if (this.selectedEnv === 'sandbox' && !this.authService.getSandboxToken()) {
      this.loading = true;
      this.authService.requestSandboxToken().subscribe({
        next: () => { this.loading = false; this.checkPrivileges(); },
        error: () => { this.loading = false; this.selectedEnv = 'production'; this.envService.setEnvironment('production'); }
      });
    } else { this.checkPrivileges(); }
  }

  onLogout(): void { this.authService.logout(); window.location.reload(); }
}
