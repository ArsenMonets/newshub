import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CreateNewsService } from './create-news.service.';
import { NewsAPI } from '../api/news.api';
import { AuthorAPI } from '../api/author.api';
import { provideRouter } from '@angular/router';

describe('CreateNews Integration', () => {
  let service: CreateNewsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CreateNewsService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        NewsAPI,
        AuthorAPI
      ]
    });
    service = TestBed.inject(CreateNewsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load categories for form', () => {
    // Constructor triggers categories load
    const catReq = httpMock.expectOne('/api/v1/public/categories');
    catReq.flush([{ id: 1, name: 'Tech' }]);
    
    expect(service.categories().length).toBe(1);
  });

  it('should create news successfully', () => {
    httpMock.expectOne('/api/v1/public/categories').flush([]);
    
    service.newsInput = { title: 'T', content: 'C', categoryId: 1 };
    service.onCreate();
    
    const req = httpMock.expectOne({ method: 'POST', url: '/api/v1/author/news' });
    req.flush({ id: 10, title: 'T' });
    
    expect(req.request.method).toBe('POST');
  });
});
