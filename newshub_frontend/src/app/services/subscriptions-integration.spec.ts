import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SubscriptionsService } from './subscriptions.service';
import { UserAPI } from '../api/user.api';

describe('Subscriptions Integration', () => {
  let fixture: ComponentFixture<SubscriptionsService>;
  let component: SubscriptionsService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubscriptionsService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        UserAPI
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SubscriptionsService);
    component = fixture.componentInstance;
  });

  afterEach(() => httpMock.verify());

  it('should load subscriptions on init', () => {
    const mockSubs = { categories: [{ id: 1, name: 'Tech' }], authors: [] };
    fixture.detectChanges();
    const req = httpMock.expectOne('/api/v1/user/subscriptions');
    req.flush(mockSubs);
    expect(component.subs()).toEqual(mockSubs as any);
  });

  it('should handle unsubscribe from author', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/v1/user/subscriptions').flush({ categories: [], authors: [{ id: 9, login: 'auth1' }] });
    component.unsubAuthor(9);
    const req = httpMock.expectOne({ method: 'DELETE', url: '/api/v1/user/unsubscribe/author/9' });
    req.flush({ categories: [], authors: [] });
    expect(component.subs()?.authors.length).toBe(0);
  });
});
