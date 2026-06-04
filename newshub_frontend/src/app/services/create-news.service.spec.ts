import { TestBed } from '@angular/core/testing';
import { CreateNewsService } from './create-news.service.';
import { AuthorAPI } from '../api/author.api';
import { NewsAPI } from '../api/news.api';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { NewsDTO } from '../models/models';

describe('CreateNewsService', () => {
  let service: CreateNewsService;
  let authorSpy: jasmine.SpyObj<AuthorAPI>;
  let newsSpy: jasmine.SpyObj<NewsAPI>;
  let mockRouter: {
    navigate: jasmine.Spy;
    lastSuccessfulNavigation: jasmine.Spy;
  };

  const mockNewsDto: NewsDTO = {
    id: 1,
    title: 'Test Title',
    summary: 'Test Summary',
    content: 'Test Content',
    category: { id: 1, name: 'Test' }
  } as any;

  beforeEach(() => {
    authorSpy = jasmine.createSpyObj('AuthorAPI', ['createNews', 'updateNews']);
    newsSpy = jasmine.createSpyObj('NewsAPI', ['getCategories']);
    
    mockRouter = {
      navigate: jasmine.createSpy('navigate'),
      lastSuccessfulNavigation: jasmine.createSpy('lastSuccessfulNavigation').and.returnValue({
        extras: { state: {} } 
      })
    };

    newsSpy.getCategories.and.returnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        CreateNewsService,
        { provide: AuthorAPI, useValue: authorSpy },
        { provide: NewsAPI, useValue: newsSpy },
        { provide: Router, useValue: mockRouter }
      ]
    });

    service = TestBed.inject(CreateNewsService);
  });

  it('should load categories', () => {
    expect(newsSpy.getCategories).toHaveBeenCalled();
  });

  it('should call createNews when no editing id', () => {
    authorSpy.createNews.and.returnValue(of(mockNewsDto));
    service.newsInput = { title: 'T', content: 'C', categoryId: 1 };
    service.onCreate();
    expect(authorSpy.createNews).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should call updateNews when editing id exists', () => {
    service.editingNewsId.set(123);
    authorSpy.updateNews.and.returnValue(of(mockNewsDto));
    service.onCreate();
    expect(authorSpy.updateNews).toHaveBeenCalledWith(123, jasmine.any(Object));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should update newsInput from navigation extras', () => {
    const news = { id: 1, title: 'Old', content: 'Old C', category: { id: 5 } };
    
    mockRouter.lastSuccessfulNavigation.and.returnValue({
      extras: { state: { news } }
    });

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        CreateNewsService,
        { provide: AuthorAPI, useValue: authorSpy },
        { provide: NewsAPI, useValue: newsSpy },
        { provide: Router, useValue: mockRouter }
      ]
    });
    
    const newService = TestBed.inject(CreateNewsService);
    
    expect(newService.editingNewsId()).toBe(1);
    expect(newService.newsInput.title).toBe('Old');
    expect(newService.newsInput.content).toBe('Old C');
    expect(newService.newsInput.categoryId).toBe(5);
  });

  it('should navigate back after edit', () => {
    service.editingNewsId.set(5);
    authorSpy.updateNews.and.returnValue(of(mockNewsDto));
    service.onCreate();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});