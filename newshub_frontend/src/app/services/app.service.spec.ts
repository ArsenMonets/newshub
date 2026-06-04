import { TestBed } from '@angular/core/testing';
import { AppService } from './app.service';
import { ErrorAPI } from '../api/error.api';
import { AuthAPI } from '../api/auth.api';

describe('AppService', () => {
  let service: AppService;
  let errorSpy: any;
  let authSpy: any;

  beforeEach(() => {
    errorSpy = jasmine.createSpyObj('ErrorAPI', ['someMethod']);
    authSpy = jasmine.createSpyObj('AuthAPI', ['someMethod']);

    TestBed.configureTestingModule({
      providers: [
        AppService,
        { provide: ErrorAPI, useValue: errorSpy },
        { provide: AuthAPI, useValue: authSpy }
      ]
    });
    service = TestBed.inject(AppService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have errorAPI and authAPI injected', () => {
    expect(service.errorAPI).toBeDefined();
    expect(service.authAPI).toBeDefined();
  });
});
