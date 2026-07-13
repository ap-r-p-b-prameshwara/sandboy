import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TransactionListComponent],
  template: `
    <div class="container">
      <h1>QRIS Transactions</h1>
      <app-transaction-list></app-transaction-list>
    </div>
  `,
  styles: [`
    .container { padding: 20px; }
    h1 { color: #333; }
  `]
})
export class AppComponent {}

