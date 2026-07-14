import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { QrisService } from '../../services/qris.service';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    MatCardModule,
    MatButtonModule
  ],
  template: `
    <div class="transaction-list">
      <div class="transactions-header">
        <h2 class="transactions-title">
          <mat-icon class="title-icon">receipt_long</mat-icon>
          Transactions
        </h2>
        <button mat-stroked-button color="primary" (click)="loadTransactions()">
          <mat-icon>refresh</mat-icon>
          Refresh
        </button>
      </div>

      <ng-container *ngIf="transactions.length > 0; else emptyState">
        <div class="table-wrapper">
          <table mat-table [dataSource]="transactions" class="transactions-table">
            <ng-container matColumnDef="id">
              <th mat-header-cell *matHeaderCellDef>ID</th>
              <td mat-cell *matCellDef="let transaction">{{ transaction.id }}</td>
            </ng-container>

            <ng-container matColumnDef="amount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let transaction">{{ transaction.amount }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let transaction">
                <span class="status-pill" [ngClass]="statusClass(transaction.status)">
                  {{ transaction.status }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="date">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let transaction">{{ transaction.date }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        </div>
      </ng-container>

      <ng-template #emptyState>
        <div class="empty-state">
          <mat-icon class="empty-icon">inbox</mat-icon>
          <p>No transactions found</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .transaction-list { width: 100%; }
    .transactions-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }
    .transactions-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 1.25rem;
      font-weight: 500;
    }
    .title-icon { color: #3f51b5; }
    .table-wrapper { overflow-x: auto; }
    .transactions-table { width: 100%; }
    .status-pill {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .status-pill.status-success {
      background: #e8f5e9;
      color: #2e7d32;
    }
    .status-pill.status-pending {
      background: #fff8e1;
      color: #ef6c00;
    }
    .status-pill.status-failed,
    .status-pill.status-error {
      background: #ffebee;
      color: #c62828;
    }
    .status-pill.status-default {
      background: #eceff1;
      color: #455a64;
    }
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 16px;
      color: rgba(0, 0, 0, 0.54);
    }
    .empty-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 8px;
    }
  `]
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];
  displayedColumns = ['id', 'amount', 'status', 'date'];

  constructor(private qrisService: QrisService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.qrisService.getTransactions().subscribe({
      next: (data) => {
        this.transactions = data;
      },
      error: (error) => {
        console.error('Failed to load transactions', error);
      }
    });
  }

  statusClass(status: string): string {
    if (!status) return 'status-default';
    const s = status.toLowerCase();
    if (s === 'success' || s === 'paid' || s === 'completed') return 'status-success';
    if (s === 'pending' || s === 'processing') return 'status-pending';
    if (s === 'failed' || s === 'error' || s === 'expired') return 'status-failed';
    return 'status-default';
  }
}
