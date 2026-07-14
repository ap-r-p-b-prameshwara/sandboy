import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { QrisService } from './qris.service';

describe('QrisService', () => {
  let service: QrisService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [QrisService]
    });
    service = TestBed.inject(QrisService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getTransactions', () => {
    it('should send GET request to /qris/transactions and return transactions', () => {
      const mockTransactions = [
        { id: '1', amount: 50000, status: 'completed' },
        { id: '2', amount: 25000, status: 'pending' }
      ];

      service.getTransactions().subscribe(transactions => {
        expect(transactions).toEqual(mockTransactions);
        expect(transactions.length).toBe(2);
      });

      const req = httpMock.expectOne('http://localhost:8080/qris/transactions');
      expect(req.request.method).toBe('GET');
      req.flush(mockTransactions);
    });

    it('should handle error when fetching transactions', () => {
      service.getTransactions().subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne('http://localhost:8080/qris/transactions');
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getTransaction', () => {
    it('should send GET request to /qris/transactions/:id and return transaction', () => {
      const mockTransaction = { id: '123', amount: 100000, status: 'completed' };

      service.getTransaction('123').subscribe(transaction => {
        expect(transaction).toEqual(mockTransaction);
      });

      const req = httpMock.expectOne('http://localhost:8080/qris/transactions/123');
      expect(req.request.method).toBe('GET');
      req.flush(mockTransaction);
    });

    it('should handle error when transaction is not found', () => {
      service.getTransaction('nonexistent').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne('http://localhost:8080/qris/transactions/nonexistent');
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });
});
