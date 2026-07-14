import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { EnvironmentService } from './environment.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let envService: EnvironmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, EnvironmentService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    envService = TestBed.inject(EnvironmentService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('login', () => {
    it('should send POST request to auth/login with username and password', () => {
      const mockResponse = { token: 'test-token-123' };

      service.login('testuser', 'testpass').subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8080/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ username: 'testuser', password: 'testpass' });
      req.flush(mockResponse);
    });

    it('should handle login error', () => {
      service.login('baduser', 'badpass').subscribe({
        error: (error) => {
          expect(error.status).toBe(401);
        }
      });

      const req = httpMock.expectOne('http://localhost:8080/auth/login');
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('register', () => {
    it('should send POST request to auth/register with username, password, and email', () => {
      const mockResponse = { token: 'new-user-token' };

      service.register('newuser', 'newpass', 'new@example.com').subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne('http://localhost:8080/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        username: 'newuser',
        password: 'newpass',
        email: 'new@example.com'
      });
      req.flush(mockResponse);
    });
  });

  describe('setToken', () => {
    it('should store token in localStorage', () => {
      service.setToken('my-token');
      expect(localStorage.getItem('token')).toBe('my-token');
    });
  });

  describe('getToken', () => {
    it('should return token from localStorage when token exists', () => {
      localStorage.setItem('token', 'stored-token');
      expect(service.getToken()).toBe('stored-token');
    });

    it('should return null when no token exists', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  describe('logout', () => {
    it('should remove token from localStorage', () => {
      localStorage.setItem('token', 'some-token');
      service.logout();
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'valid-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when no token exists', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });
  });
});
