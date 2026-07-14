import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule
  ],
  template: `
    <div class="register-container">
      <mat-card-header class="register-header">
        <div mat-card-avatar class="register-avatar">
          <mat-icon>person_add</mat-icon>
        </div>
        <mat-card-title>Register</mat-card-title>
        <mat-card-subtitle>Create a new Sandbox account</mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <form (ngSubmit)="onRegister()">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Username</mat-label>
            <input matInput type="text" [(ngModel)]="username" name="username" required>
            <mat-icon matSuffix>person</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email</mat-label>
            <input matInput type="email" [(ngModel)]="email" name="email" required>
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
        <button mat-button type="button" (click)="onCancel()">Cancel</button>
        <button mat-raised-button color="primary" type="button" (click)="onRegister()">
          <mat-icon>how_to_reg</mat-icon>
          Register
        </button>
      </mat-card-actions>
    </div>
  `,
  styles: [`
    .register-container { width: 100%; }
    .register-header { padding: 16px 16px 0; }
    .register-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e3f2fd;
      color: #1976d2;
    }
    .full-width {
      width: 100%;
      margin-top: 8px;
    }
  `]
})
export class RegisterComponent {
  @Output() registered = new EventEmitter<void>();

  username = '';
  email = '';
  password = '';

  constructor(private authService: AuthService) {}

  onRegister(): void {
    this.authService.register(this.username, this.password, this.email).subscribe({
      next: () => {
        this.registered.emit();
      },
      error: (error) => {
        console.error('Registration failed', error);
      }
    });
  }

  onCancel(): void {
    this.registered.emit();
  }
}
