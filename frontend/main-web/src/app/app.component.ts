import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { EnvironmentService } from './services/environment.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, LoginComponent, RegisterComponent, HomeComponent],
  template: `
    <!-- Production: show login/register -->
    <ng-container *ngIf="envService.environment === 'production'">
      <app-login *ngIf="!isLoggedIn"></app-login>
      <app-home *ngIf="isLoggedIn"></app-home>
    </ng-container>
    
    <!-- Sandbox: require login from production -->
    <ng-container *ngIf="envService.environment === 'sandbox'">
      <app-home *ngIf="isLoggedIn"></app-home>
      <div *ngIf="!isLoggedIn" class="sandbox-redirect">
        <h2>Sandbox Mode</h2>
        <p>Please login from Production environment first.</p>
        <button (click)="switchToProduction()">Switch to Production</button>
      </div>
    </ng-container>
  `,
  styles: [`
    .sandbox-redirect { text-align: center; padding: 2rem; }
    .sandbox-redirect button { margin-top: 1rem; padding: 0.5rem 1rem; cursor: pointer; }
  `]
})
export class AppComponent {
  isLoggedIn = false;

  constructor(public envService: EnvironmentService) {
    this.isLoggedIn = !!localStorage.getItem('token');
  }

  switchToProduction(): void {
    this.envService.setEnvironment('production');
  }
}

