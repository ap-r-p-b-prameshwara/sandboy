import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TransactionListComponent],
  template: `
    <div class="container">
      <div class="header">
        <img src="assets/sandboy.png" alt="Sandboy Logo" class="logo">
        <h1>QRIS Transactions</h1>
      </div>
      <app-transaction-list></app-transaction-list>
    </div>
  `,
  styles: [`
    .container { padding: 20px; }
    .header { display: flex; align-items: center; }
    .logo { width: 70px; height: auto; display: inline-block; margin-right: 12px; }
    h1 { color: #333; margin: 0; }
  `]
})
export class AppComponent {}

