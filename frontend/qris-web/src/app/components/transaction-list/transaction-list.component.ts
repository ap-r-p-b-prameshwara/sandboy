import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { QrisService } from '../../services/qris.service';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="transaction-list">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let transaction of transactions">
            <td>{{ transaction.id }}</td>
            <td>{{ transaction.amount }}</td>
            <td>{{ transaction.status }}</td>
            <td>{{ transaction.date }}</td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="transactions.length === 0">No transactions found</p>
    </div>
  `,
  styles: [`
    table { width: 100%; border-collapse: collapse; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background-color: #f2f2f2; }
  `]
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];

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
}

