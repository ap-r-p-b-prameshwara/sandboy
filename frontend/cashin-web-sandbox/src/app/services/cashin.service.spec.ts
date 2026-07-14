import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CashinService } from './cashin.service';

describe('CashinService', () => {
  let service: CashinService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CashinService]
    });
    service = TestBed.inject(CashinService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getVirtualAccounts', () => {
    it('should send GET request to /cashin/va and return virtual accounts', () => {
      const mockAccounts = [
        { id: 'va-1', bank: 'BCA', accountNumber: '1234567890' },
        { id: 'va-2', bank: 'BNI', accountNumber: '0987654321' }
      ];

      service.getVirtualAccounts().subscribe(accounts => {
        expect(accounts).toEqual(mockAccounts);
        expect(accounts.length).toBe(2);
      });

      const req = httpMock.expectOne('http://localhost:8080/cashin/va');
      expect(req.request.method).toBe('GET');
      req.flush(mockAccounts);
    });

    it('should handle error when fetching virtual accounts', () => {
      service.getVirtualAccounts().subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne('http://localhost:8080/cashin/va');
      req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getVirtualAccount', () => {
    it('should send GET request to /cashin/va/:id and return virtual account', () => {
      const mockAccount = { id: 'va-1', bank: 'BCA', accountNumber: '1234567890' };

      service.getVirtualAccount('va-1').subscribe(account => {
        expect(account).toEqual(mockAccount);
      });

      const req = httpMock.expectOne('http://localhost:8080/cashin/va/va-1');
      expect(req.request.method).toBe('GET');
      req.flush(mockAccount);
    });

    it('should handle error when virtual account is not found', () => {
      service.getVirtualAccount('nonexistent').subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne('http://localhost:8080/cashin/va/nonexistent');
      req.flush('Not Found', { status: 404, statusText: 'Not Found' });
    });
  });
});
