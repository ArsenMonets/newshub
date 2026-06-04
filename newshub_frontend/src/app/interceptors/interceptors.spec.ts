import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { ErrorInterceptor, AuthInterceptor } from './interceptors';
import { ErrorAPI } from '../api/error.api';

describe('Interceptors', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let errorAPI: jasmine.SpyObj<ErrorAPI>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ErrorAPI', ['setError']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
        { provide: ErrorAPI, useValue: spy }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    errorAPI = TestBed.inject(ErrorAPI) as jasmine.SpyObj<ErrorAPI>;
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('AuthInterceptor', () => {
    it('should add Authorization header if token exists', () => {
      localStorage.setItem('token', 'test-token');
      http.get('/api/test').subscribe();
      const req = httpMock.expectOne('/api/test');
      expect(req.request.headers.has('Authorization')).toBeTrue();
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
    });

    it('should NOT add Authorization header if no token', () => {
      http.get('/api/test').subscribe();
      const req = httpMock.expectOne('/api/test');
      expect(req.request.headers.has('Authorization')).toBeFalse();
    });
  });

  describe('ErrorInterceptor', () => {
    it('should catch string error and call ErrorAPI', () => {
      http.get('/api/test').subscribe({ error: () => {} });
      const req = httpMock.expectOne('/api/test');
      req.flush('Custom error message', { status: 400, statusText: 'Bad Request' });
      expect(errorAPI.setError).toHaveBeenCalledWith('Custom error message');
    });

    it('should parse JSON error messages', () => {
      http.get('/api/test').subscribe({ error: () => {} });
      const req = httpMock.expectOne('/api/test');
      req.flush(JSON.stringify({ message: 'JSON Error' }), { status: 400, statusText: 'Bad Request' });
      expect(errorAPI.setError).toHaveBeenCalledWith('JSON Error');
    });

    it('should handle HTML error responses gracefully', () => {
      http.get('/api/test').subscribe({ error: () => {} });
      const req = httpMock.expectOne('/api/test');
      req.flush('<html><body>error</body></html>', { status: 500, statusText: 'Internal Server Error' });
      expect(errorAPI.setError).toHaveBeenCalledWith('Cannot GET test (500)');
    });
  });
});
