import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RegisterService } from './auth.service';
import { AuthAPI } from '../api/auth.api';

describe('Auth Integration (Register)', () => {
  let fixture: ComponentFixture<RegisterService>;
  let component: RegisterService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthAPI
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterService);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('should register user and navigate to home page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.regData = { login: 'newuser', email: 'test@test.com', password: 'password123' };
    
    component.onRegister();
    
    const req = httpMock.expectOne({ method: 'POST', url: '/api/v1/auth/register' });
    expect(req.request.body).toEqual(component.regData);
    
    req.flush({ token: 'new-token', user: { id: 2, login: 'newuser' } });
    
    expect(component.loading()).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});
