import { TestBed } from '@angular/core/testing';
import { LoginService, RegisterService } from './auth.service';
import { AuthAPI } from '../api/auth.api';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('AuthServices', () => {
  let authSpy: any;
  let routerSpy: any;

  beforeEach(() => {
    authSpy = jasmine.createSpyObj('AuthAPI', ['login', 'register']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    
    TestBed.configureTestingModule({
      providers: [
        LoginService,
        RegisterService,
        { provide: AuthAPI, useValue: authSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  describe('LoginService', () => {
    it('should login and navigate', () => {
      const service = TestBed.inject(LoginService);
      authSpy.login.and.returnValue(of({}));
      service.loginData = { login: 'u', password: 'p' };
      service.onLogin();
      expect(authSpy.login).toHaveBeenCalledWith(service.loginData);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
      expect(service.loading()).toBeFalse();
    });

    it('should handle login error', () => {
      const service = TestBed.inject(LoginService);
      authSpy.login.and.returnValue(throwError(() => 'err'));
      service.onLogin();
      expect(service.loading()).toBeFalse();
    });

    it('should set loading to true while logging in', () => {
      const service = TestBed.inject(LoginService);
      authSpy.login.and.returnValue(of({}));
      service.onLogin();
      expect(service.loading()).toBeDefined();
    });
  });

  describe('RegisterService', () => {
    it('should register and navigate', () => {
      const service = TestBed.inject(RegisterService);
      authSpy.register.and.returnValue(of({}));
      service.regData = { login: 'u', email: 'e', password: 'p' };
      service.onRegister();
      expect(authSpy.register).toHaveBeenCalledWith(service.regData);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should handle register error', () => {
      const service = TestBed.inject(RegisterService);
      authSpy.register.and.returnValue(throwError(() => 'err'));
      service.onRegister();
      expect(service.loading()).toBeFalse();
    });

    it('should set loading to true while registering', () => {
      const service = TestBed.inject(RegisterService);
      authSpy.register.and.returnValue(of({}));
      service.onRegister();
      expect(service.loading()).toBeDefined();
    });

    it('should initialize with empty regData', () => {
      const service = TestBed.inject(RegisterService);
      expect(service.regData.login).toBe('');
    });
  });
});
