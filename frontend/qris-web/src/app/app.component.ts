import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    TransactionListComponent,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <img src="assets/sandboy.png" alt="Sandboy Logo" class="toolbar-logo">
      <span class="toolbar-title">QRIS Transactions</span>
    </mat-toolbar>

    <div class="container">
      <mat-card class="content-card">
        <mat-card-content>
          <app-transaction-list></app-transaction-list>
        </mat-card-content>
      </mat-card>
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
    .content-card { margin-top: 20px; }
  `]
})
export class AppComponent {}
