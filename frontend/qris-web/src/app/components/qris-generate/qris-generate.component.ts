import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { QrisService } from '../../services/qris.service';

@Component({
  selector: 'app-qris-generate',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './qris-generate.component.html',
  styleUrl: './qris-generate.component.scss',
})
export class QrisGenerateComponent {
  private qrisService = inject(QrisService);
  
  amount = 0;
  qrCode = '';
  loading = false;
  error = '';
  isSandbox = false;
  
  constructor() {
    this.isSandbox = localStorage.getItem('environment') === 'sandbox';
  }
  
  generateQr(): void {
    if (this.amount <= 0) {
      this.error = 'Amount must be greater than 0';
      return;
    }
    
    this.loading = true;
    this.error = '';
    this.qrCode = '';
    
    this.qrisService.generateQris(this.amount).subscribe({
      next: (response) => {
        this.qrCode = response.qrCode;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to generate QRIS';
        this.loading = false;
      },
    });
  }
}
