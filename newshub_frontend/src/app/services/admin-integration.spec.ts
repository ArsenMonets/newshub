import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { AdminAPI } from '../api/admin.api';
import { NewsAPI } from '../api/news.api';

describe('Admin Integration (User Management)', () => {
  let fixture: ComponentFixture<AdminService>;
  let component: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        AdminAPI,
        NewsAPI
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AdminService);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should block user and update the users signal', () => {
    httpMock.expectOne('/api/v1/public/categories').flush([]);
    httpMock.expectOne(req => req.url.includes('/api/v1/admin/users')).flush({ content: [{ id: 1, login: 'user1', isBlocked: false }], totalElements: 1 });

    const user = { id: 1, login: 'user1', isBlocked: false } as any;
    component.users.set([user]);

    component.toggleBlock(user, true);

    const blockReq = httpMock.expectOne({ method: 'POST', url: '/api/v1/admin/users/1/block' });
    blockReq.flush({ ...user, isBlocked: true });

    expect(component.users()[0].isBlocked).toBeTrue();
  });

  it('should add a new category and refresh list', () => {
    httpMock.expectOne('/api/v1/public/categories').flush([]);
    httpMock.expectOne(req => req.url.includes('/api/v1/admin/users')).flush({ content: [], totalElements: 0 });

    component.addCategory('Technology');

    const addReq = httpMock.expectOne(req => req.method === 'POST' && req.url === '/api/v1/admin/categories' && req.params.get('name') === 'Technology');
    addReq.flush({ id: 1, name: 'Technology' });

    httpMock.expectOne('/api/v1/public/categories').flush([{ id: 1, name: 'Technology' }]);
    httpMock.expectOne(req => req.url.includes('/api/v1/admin/users')).flush({ content: [], totalElements: 0 });

    expect(component.categories().length).toBe(1);
    expect(component.categories()[0].name).toBe('Technology');
  });
});
