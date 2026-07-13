import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="register-container">
      <h3>Register</h3>
      <form (ngSubmit)="onRegister()">
        <input type="text" [(ngModel)]="username" name="username" placeholder="Username" required>
        <input type="email" [(ngModel)]="email" name="email" placeholder="Email" required>
        <input type="password" [(ngModel)]="password" name="password" placeholder="Password" required>
        <button type="submit">Register</button>
        <button type="button" (click)="onCancel()">Cancel</button>
      </form>
    </div>
  `,
  styles: [`
    .register-container { margin-top: 20px; padding: 20px; border: 1px solid #ccc; }
    input { display: block; margin: 10px 0; padding: 10px; width: 100%; }
    button { padding: 10px 20px; margin: 5px; }
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

