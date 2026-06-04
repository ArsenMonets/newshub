import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { routes } from '../app.routes';
import { LoginService } from './auth.service';
import { AuthAPI } from '../api/auth.api';
import { By } from '@angular/platform-browser';

describe('Auth Integration (Login)', () => {
  let fixture: ComponentFixture<LoginService>;
  let component: LoginService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginService], // Standalone component
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter(routes),
        AuthAPI
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginService);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should login user and navigate to home page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    
    // Simulate user input
    component.loginData = { login: 'admin', password: 'password123' };
    
    // Trigger login
    component.onLogin();
    
    // Expect HTTP call
    const req = httpMock.expectOne({ method: 'POST', url: '/api/v1/auth/login' });
    expect(req.request.body).toEqual(component.loginData);
    
    // Return success response
    req.flush({ token: 'fake-jwt-token', user: { id: 1, login: 'admin' } });
    
    // Check results
    expect(component.loading()).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});
