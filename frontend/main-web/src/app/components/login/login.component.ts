import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="logo-header">
        <img src="assets/sandboy.png" alt="Sandboy Logo" class="logo">
        <h2>Login</h2>
      </div>
      <form (ngSubmit)="onLogin()">
        <input type="text" [(ngModel)]="username" name="username" placeholder="Username" required>
        <input type="password" [(ngModel)]="password" name="password" placeholder="Password" required>
        <button type="submit">Login</button>
      </form>
      <button (click)="showRegister = true">Register</button>
      <app-register *ngIf="showRegister" (registered)="showRegister = false"></app-register>
    </div>
  `,
  styles: [`
    .login-container { max-width: 400px; margin: 50px auto; padding: 20px; }
    .logo-header { display: flex; align-items: center; margin-bottom: 20px; }
    .logo { width: 70px; height: auto; display: inline-block; margin-right: 12px; }
    .logo-header h2 { margin: 0; }
    input { display: block; margin: 10px 0; padding: 10px; width: 100%; }
    button { padding: 10px 20px; margin: 5px; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  showRegister = false;

  constructor(private authService: AuthService) {}

  onLogin(): void {
    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        this.authService.setToken(response.token);
        window.location.reload();
      },
      error: (error) => {
        console.error('Login failed', error);
      }
    });
  }
}

