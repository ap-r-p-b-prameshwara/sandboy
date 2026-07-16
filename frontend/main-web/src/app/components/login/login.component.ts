import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RegisterComponent } from '../register/register.component';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RegisterComponent,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <div mat-card-avatar class="logo-avatar">
            <img src="assets/sandboy.png" alt="Sandboy Logo">
          </div>
          <mat-card-title>Login</mat-card-title>
          <mat-card-subtitle>Sign in to your Sandbox account</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form (ngSubmit)="onLogin()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput [(ngModel)]="username" name="username" required>
              <mat-icon matSuffix>email</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput type="password" [(ngModel)]="password" name="password" required>
              <mat-icon matSuffix>lock</mat-icon>
            </mat-form-field>
          </form>
        </mat-card-content>
        <mat-card-actions align="end">
          <button mat-button (click)="showRegister = true">Register</button>
          <button mat-raised-button color="primary" (click)="onLogin()">
            <mat-icon>login</mat-icon>
            Login
          </button>
        </mat-card-actions>
      </mat-card>

      <mat-card *ngIf="showRegister" class="register-card">
        <app-register (registered)="showRegister = false"></app-register>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 420px;
      margin: 60px auto;
      padding: 20px;
    }
    .login-card,
    .register-card {
      margin-bottom: 20px;
    }
    .logo-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      background: transparent;
    }
    .logo-avatar img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
    .full-width {
      width: 100%;
      margin-top: 8px;
    }
    mat-card-actions {
      padding: 8px 16px 16px;
    }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  showRegister = false;

  constructor(private authService: AuthService) {}

  onLogin(): void {
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        window.location.reload();
      },
      error: (error) => {
        console.error('Login failed', error);
      }
    });
  }
}
