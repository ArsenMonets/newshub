import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { MyNewsService } from './my-news.service';
import { NewsAPI } from '../api/news.api';
import { AuthorAPI } from '../api/author.api';
import { AuthAPI } from '../api/auth.api';
import { signal } from '@angular/core';

describe('MyNews Integration', () => {
  let fixture: ComponentFixture<MyNewsService>;
  let component: MyNewsService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyNewsService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        NewsAPI,
        AuthorAPI
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MyNewsService);
    component = fixture.componentInstance;
  });

  afterEach(() => httpMock.verify());

  it('should load my news and delete one', () => {
    const user = { id: 10, login: 'author1' } as any;
    const authAPI = TestBed.inject(AuthAPI);
    (authAPI as any).user = signal(user);

    fixture.detectChanges(); 
    
    const req = httpMock.expectOne(request => request.url.includes('/api/v1/public/news/filter'));
    req.flush({ content: [{ id: 1, title: 'My News' }], totalPages: 1 });

    const newsToDelete = component.myNews()[0];
    spyOn(window, 'confirm').and.returnValue(true);
    component.deleteNews(newsToDelete);
    
    const delReq = httpMock.expectOne({ method: 'DELETE', url: '/api/v1/author/news/1' });
    delReq.flush('', { status: 204, statusText: 'No Content' });
    
    expect(component.myNews().length).toBe(0);
  });
});
