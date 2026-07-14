import { TestBed } from '@angular/core/testing';
import { EnvironmentService } from './environment.service';

describe('EnvironmentService', () => {
  let service: EnvironmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EnvironmentService]
    });
    service = TestBed.inject(EnvironmentService);
  });

  it('should default to production environment', () => {
    expect(service.environment).toBe('production');
  });

  describe('setEnvironment', () => {
    it('should set environment to production', () => {
      service.setEnvironment('production');
      expect(service.environment).toBe('production');
    });

    it('should set environment to sandbox', () => {
      service.setEnvironment('sandbox');
      expect(service.environment).toBe('sandbox');
    });
  });

  describe('apiUrl', () => {
    it('should return production URL when environment is production', () => {
      service.setEnvironment('production');
      expect(service.apiUrl).toBe('http://localhost:8080');
    });

    it('should return sandbox URL when environment is sandbox', () => {
      service.setEnvironment('sandbox');
      expect(service.apiUrl).toBe('http://localhost:8085');
    });

    it('should update apiUrl after switching environment', () => {
      expect(service.apiUrl).toBe('http://localhost:8080');
      service.setEnvironment('sandbox');
      expect(service.apiUrl).toBe('http://localhost:8085');
      service.setEnvironment('production');
      expect(service.apiUrl).toBe('http://localhost:8080');
    });
  });

  describe('environment getter', () => {
    it('should return current environment value', () => {
      expect(service.environment).toBe('production');
      service.setEnvironment('sandbox');
      expect(service.environment).toBe('sandbox');
    });
  });
});
