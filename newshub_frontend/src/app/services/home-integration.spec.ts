import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { HomeService } from './home.service';
import { NewsAPI } from '../api/news.api';
import { AuthAPI } from '../api/auth.api';
import { UserAPI } from '../api/user.api';
import { WebSocketAPI } from '../api/websocket.api';
import { ErrorAPI } from '../api/error.api';
import { ErrorInterceptor } from '../interceptors/interceptors';
import { signal } from '@angular/core';
import { of } from 'rxjs';

describe('Home Integration (Data Loading)', () => {
  let fixture: ComponentFixture<HomeService>;
  let component: HomeService;
  let httpMock: HttpTestingController;
  let errorAPI: jasmine.SpyObj<ErrorAPI>;

  beforeEach(async () => {
    const errorSpy = jasmine.createSpyObj('ErrorAPI', ['setError']);
    const authSpy = { user: signal(null) };
    const wsSpy = {
      newsCreated$: of(),
      newsUpdated$: of(),
      newsDeleted$: of()
    };

    await TestBed.configureTestingModule({
      imports: [HomeService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
        { provide: ErrorAPI, useValue: errorSpy },
        { provide: AuthAPI, useValue: authSpy },
        { provide: WebSocketAPI, useValue: wsSpy },
        NewsAPI,
        UserAPI
      ]
    }).compileComponents();

    errorAPI = TestBed.inject(ErrorAPI) as jasmine.SpyObj<ErrorAPI>;
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(HomeService);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show error message when categories fail to load', () => {
    const catReq = httpMock.expectOne('/api/v1/public/categories');
    const authReq = httpMock.expectOne('/api/v1/public/authors');
    const newsReq = httpMock.expectOne(req => req.url.includes('/api/v1/public/news/filter'));

    catReq.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
    authReq.flush([]);
    newsReq.flush({ content: [], totalPages: 0 });

    expect(errorAPI.setError).toHaveBeenCalled();
  });

  it('should correctly filter news and update state', () => {
    httpMock.expectOne('/api/v1/public/categories').flush([]);
    httpMock.expectOne('/api/v1/public/authors').flush([]);
    httpMock.expectOne(req => req.url.includes('/api/v1/public/news/filter')).flush({ content: [], totalPages: 0 });

    component.searchQuery = 'angular';
    component.loadNews();

    const searchReq = httpMock.expectOne(req => req.url === '/api/v1/public/news/search' && req.params.get('query') === 'angular');
    const dummyNews = { content: [{ id: 1, title: 'Test News' }], totalPages: 1 };
    searchReq.flush(dummyNews);

    expect(component.newsItems().length).toBe(1);
    expect(component.newsItems()[0].title).toBe('Test News');
  });
});
