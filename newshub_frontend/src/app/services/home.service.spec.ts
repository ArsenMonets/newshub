import { TestBed } from '@angular/core/testing';
import { HomeService } from './home.service';
import { NewsAPI } from '../api/news.api';
import { UserAPI } from '../api/user.api';
import { AuthAPI } from '../api/auth.api';
import { WebSocketAPI } from '../api/websocket.api';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('HomeService', () => {
  let service: HomeService;
  let newsSpy: any;
  let userSpy: any;
  let authSpy: any;
  let wsSpy: any;

  beforeEach(() => {
    newsSpy = jasmine.createSpyObj('NewsAPI', ['getCategories', 'getAuthors', 'searchNews', 'filterNews']);
    userSpy = jasmine.createSpyObj('UserAPI', ['getSubscriptions']);
    wsSpy = {
      newsCreated$: of(),
      newsUpdated$: of(),
      newsDeleted$: of(),
      setup: jasmine.createSpy('setup')
    };
    authSpy = { user: signal(null) };

    newsSpy.getCategories.and.returnValue(of([]));
    newsSpy.getAuthors.and.returnValue(of([]));
    newsSpy.filterNews.and.returnValue(of({ content: [], totalPages: 0 }));

    TestBed.configureTestingModule({
      providers: [
        HomeService,
        { provide: NewsAPI, useValue: newsSpy },
        { provide: UserAPI, useValue: userSpy },
        { provide: AuthAPI, useValue: authSpy },
        { provide: WebSocketAPI, useValue: wsSpy }
      ]
    });
    service = TestBed.inject(HomeService);
  });

  it('should load initial data', () => {
    expect(newsSpy.getCategories).toHaveBeenCalled();
    expect(newsSpy.getAuthors).toHaveBeenCalled();
  });

  it('should load subscriptions if user logged in', () => {
    authSpy.user.set({ id: 1 } as any);
    userSpy.getSubscriptions.and.returnValue(of({ categories: [], authors: [] }));
    service.loadInitialData();
    expect(userSpy.getSubscriptions).toHaveBeenCalled();
  });

  it('should check subscriptions', () => {
    service.subscribedCategoryIds.set([1]);
    expect(service.isSubscribedToCategory(1)).toBeTrue();
    expect(service.isSubscribedToCategory(2)).toBeFalse();
  });

  it('should load news with search query', () => {
    service.searchQuery = 'test';
    newsSpy.searchNews.and.returnValue(of({ content: [], totalPages: 1 }));
    service.loadNews();
    expect(newsSpy.searchNews).toHaveBeenCalledWith('test', 0);
  });

  it('should load all news if not logged in', () => {
    authSpy.user.set(null);
    service.loadNews();
    expect(newsSpy.filterNews).toHaveBeenCalled();
  });

  it('should handle pagination', () => {
    service.page.set(1);
    service.loadNews();
    expect(newsSpy.filterNews).toHaveBeenCalledWith([], [], 1);
  });

  it('should toggle showAll and load news', () => {
    service.showAll.set(true);
    service.loadNews();
    expect(newsSpy.filterNews).toHaveBeenCalled();
  });

  it('should clear news if no subscriptions found and user logged in', () => {
    authSpy.user.set({ id: 1 } as any);
    userSpy.getSubscriptions.and.returnValue(of({ categories: [], authors: [] }));
    service.loadNews();
    expect(service.newsItems()).toEqual([]);
  });

  it('should load user subscriptions correctly', () => {
    const subs = { categories: [{ id: 10 }], authors: [{ id: 20 }] };
    userSpy.getSubscriptions.and.returnValue(of(subs));
    service.loadUserSubscriptions();
    expect(service.subscribedCategoryIds()).toContain(10);
    expect(service.subscribedAuthorIds()).toContain(20);
  });

  it('should check if is subscribed to author', () => {
    service.subscribedAuthorIds.set([5]);
    expect(service.isSubscribedToAuthor(5)).toBeTrue();
    expect(service.isSubscribedToAuthor(1)).toBeFalse();
  });
});
