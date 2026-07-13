import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VaListComponent } from './components/va-list/va-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, VaListComponent],
  template: `
    <div class="container">
      <div class="header">
        <img src="assets/sandboy.png" alt="Sandboy Logo" class="logo">
        <h1>Virtual Accounts</h1>
      </div>
      <app-va-list></app-va-list>
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

