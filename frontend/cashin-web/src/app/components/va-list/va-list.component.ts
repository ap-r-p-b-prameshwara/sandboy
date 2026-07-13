import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CashinService } from '../../services/cashin.service';

@Component({
  selector: 'app-va-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="va-list">
      <table>
        <thead>
          <tr>
            <th>VA Number</th>
            <th>Bank</th>
            <th>Status</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let va of virtualAccounts">
            <td>{{ va.number }}</td>
            <td>{{ va.bank }}</td>
            <td>{{ va.status }}</td>
            <td>{{ va.balance }}</td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="virtualAccounts.length === 0">No virtual accounts found</p>
    </div>
  `,
  styles: [`
    table { width: 100%; border-collapse: collapse; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background-color: #f2f2f2; }
  `]
})
export class VaListComponent implements OnInit {
  virtualAccounts: any[] = [];

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
}

