import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { NewsDetailService } from './news-detail.service';
import { NewsAPI } from '../api/news.api';
import { of } from 'rxjs';

describe('NewsDetail Integration', () => {
  let fixture: ComponentFixture<NewsDetailService>;
  let component: NewsDetailService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewsDetailService],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: '123' }
            }
          }
        },
        NewsAPI
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(NewsDetailService);
    component = fixture.componentInstance;
  });

  afterEach(() => httpMock.verify());

  it('should load news details on init', () => {
    const mockNews = { id: 123, title: 'Integration Test News', content: 'Some content' };
    
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/v1/public/news/123');
    req.flush(mockNews);

    expect(component.news()).toEqual(mockNews as any);
  });
});
