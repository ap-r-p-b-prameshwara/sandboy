import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VaListComponent } from './components/va-list/va-list.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, VaListComponent],
  template: `
    <div class="container">
      <h1>Virtual Accounts</h1>
      <app-va-list></app-va-list>
    </div>
  `,
  styles: [`
    .container { padding: 20px; }
    h1 { color: #333; }
  `]
})
export class AppComponent {}

