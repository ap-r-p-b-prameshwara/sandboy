import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CashinService } from '../../services/cashin.service';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-va-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    MatCardModule,
    MatButtonModule
  ],
  template: `
    <div class="va-list">
      <div class="va-header">
        <h2 class="va-title">
          <mat-icon class="title-icon">account_balance_wallet</mat-icon>
          Virtual Accounts
        </h2>
        <button mat-stroked-button color="primary" (click)="loadVirtualAccounts()">
          <mat-icon>refresh</mat-icon>
          Refresh
        </button>
      </div>

      <ng-container *ngIf="virtualAccounts.length > 0; else emptyState">
        <div class="table-wrapper">
          <table mat-table [dataSource]="virtualAccounts" class="va-table">
            <ng-container matColumnDef="number">
              <th mat-header-cell *matHeaderCellDef>VA Number</th>
              <td mat-cell *matCellDef="let va">{{ va.number }}</td>
            </ng-container>

            <ng-container matColumnDef="bank">
              <th mat-header-cell *matHeaderCellDef>Bank</th>
              <td mat-cell *matCellDef="let va">{{ va.bank }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let va">
                <span class="status-pill" [ngClass]="statusClass(va.status)">
                  {{ va.status }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="balance">
              <th mat-header-cell *matHeaderCellDef>Balance</th>
              <td mat-cell *matCellDef="let va">{{ va.balance }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        </div>
      </ng-container>

      <ng-template #emptyState>
        <div class="empty-state">
          <mat-icon class="empty-icon">inbox</mat-icon>
          <p>No virtual accounts found</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .va-list { width: 100%; }
    .va-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }
    .va-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 1.25rem;
      font-weight: 500;
    }
    .title-icon { color: #3f51b5; }
    .table-wrapper { overflow-x: auto; }
    .va-table { width: 100%; }
    .status-pill {
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .status-pill.status-active {
      background: #e8f5e9;
      color: #2e7d32;
    }
    .status-pill.status-inactive,
    .status-pill.status-closed {
      background: #eceff1;
      color: #455a64;
    }
    .status-pill.status-pending {
      background: #fff8e1;
      color: #ef6c00;
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
export class VaListComponent implements OnInit {
  virtualAccounts: any[] = [];
  displayedColumns = ['number', 'bank', 'status', 'balance'];

  constructor(private cashinService: CashinService) {}

  ngOnInit(): void {
    this.loadVirtualAccounts();
  }

  loadVirtualAccounts(): void {
    this.cashinService.getVirtualAccounts().subscribe({
      next: (data) => {
        this.virtualAccounts = data;
      },
      error: (error) => {
        console.error('Failed to load virtual accounts', error);
      }
    });
  }

  statusClass(status: string): string {
    if (!status) return 'status-default';
    const s = status.toLowerCase();
    if (s === 'active') return 'status-active';
    if (s === 'inactive' || s === 'closed') return 'status-inactive';
    if (s === 'pending') return 'status-pending';
    return 'status-default';
  }
}
