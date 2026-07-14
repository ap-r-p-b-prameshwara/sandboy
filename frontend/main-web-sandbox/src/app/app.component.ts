import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './components/home/home.component';
import { EnvironmentService } from './services/environment.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    HomeComponent,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <!-- Sandbox: requires sandboxToken from production -->
    <app-home *ngIf="isLoggedIn"></app-home>
    <div *ngIf="!isLoggedIn" class="sandbox-redirect-wrapper">
      <mat-card class="sandbox-card">
        <mat-card-header>
          <div mat-card-avatar class="sandbox-avatar">
            <mat-icon>science</mat-icon>
          </div>
          <mat-card-title>Sandbox Mode</mat-card-title>
          <mat-card-subtitle>Login required from Production first</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p>Please login from Production environment first.</p>
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="goToProduction()">
            <mat-icon>swap_horiz</mat-icon>
            Go to Production
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .sandbox-redirect-wrapper {
      max-width: 480px;
      margin: 60px auto;
      padding: 20px;
    }
    .sandbox-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e3f2fd;
      color: #1976d2;
    }
  `]
})
export class AppComponent {
  isLoggedIn = false;

  constructor(public envService: EnvironmentService) {
    this.isLoggedIn = !!localStorage.getItem('sandboxToken');
  }

  goToProduction(): void {
    window.location.href = 'http://localhost:4200';
  }
}
