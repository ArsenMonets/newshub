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

  it('should handle onSearch correctly', () => {
    service.page.set(5);
    service.searchQuery = 'Query';
    newsSpy.searchNews.and.returnValue(of({ content: [], totalPages: 1 }));
    
    service.onSearch();
    
    expect(service.page()).toBe(0);
    expect(newsSpy.searchNews).toHaveBeenCalledWith('Query', 0);
  });

  it('should reset filters on toggleShowAll', () => {
    service.showAll.set(false);
    service.selectedCategories = [1, 2];
    service.searchQuery = 'some text';
    
    service.toggleShowAll();
    
    expect(service.showAll()).toBeTrue();
    expect(service.selectedCategories).toEqual([]);
    expect(service.searchQuery).toBe('');
  });

  it('should add and remove categories via toggleCategory', () => {
    service.selectedCategories = [1];
    newsSpy.filterNews.and.returnValue(of({ content: [], totalPages: 0 }));

    service.toggleCategory(2);
    expect(service.selectedCategories).toContain(2);
    expect(service.showAll()).toBeTrue();

    service.toggleCategory(1);
    expect(service.selectedCategories).not.toContain(1);
  });

  it('should add and remove authors via toggleAuthor', () => {
    service.selectedAuthors = [5];
    newsSpy.filterNews.and.returnValue(of({ content: [], totalPages: 0 }));

    service.toggleAuthor(10);
    expect(service.selectedAuthors).toContain(10);

    service.toggleAuthor(5);
    expect(service.selectedAuthors).not.toContain(5);
  });

  it('should navigate pages via prevPage and nextPage within boundaries', () => {
    newsSpy.filterNews.and.returnValue(of({ content: [], totalPages: 3 }));
    service.totalPages.set(3);
    
    service.page.set(0);
    service.nextPage();
    expect(service.page()).toBe(1);

    service.page.set(2);
    service.nextPage();
    expect(service.page()).toBe(2);

    service.page.set(2);
    service.prevPage();
    expect(service.page()).toBe(1);

    service.page.set(0);
    service.prevPage();
    expect(service.page()).toBe(0);
  });


  it('should manage category subscriptions updates', () => {
    userSpy.subscribeToCategory = jasmine.createSpy().and.returnValue(of({}));
    userSpy.unsubscribeFromCategory = jasmine.createSpy().and.returnValue(of({}));
    service.subscribedCategoryIds.set([1]);

    service.subscribeToCategory(2);
    expect(userSpy.subscribeToCategory).toHaveBeenCalledWith(2);
    expect(service.subscribedCategoryIds()).toContain(2);

    service.unsubscribeFromCategory(1);
    expect(userSpy.unsubscribeFromCategory).toHaveBeenCalledWith(1);
    expect(service.subscribedCategoryIds()).not.toContain(1);
  });

  it('should manage author subscriptions updates', () => {
    userSpy.subscribeToAuthor = jasmine.createSpy().and.returnValue(of({}));
    userSpy.unsubscribeFromAuthor = jasmine.createSpy().and.returnValue(of({}));
    service.subscribedAuthorIds.set([10]);

    service.subscribeToAuthor(20);
    expect(userSpy.subscribeToAuthor).toHaveBeenCalledWith(20);
    expect(service.subscribedAuthorIds()).toContain(20);

    service.unsubscribeFromAuthor(10);
    expect(userSpy.unsubscribeFromAuthor).toHaveBeenCalledWith(10);
    expect(service.subscribedAuthorIds()).not.toContain(10);
  });

  it('should load news based on active user subscriptions', () => {
    authSpy.user.set({ id: 1 } as any);
    service.searchQuery = '';
    service.showAll.set(false);
    
    const mockSubs = { 
      categories: [{ id: 1 }], 
      authors: [{ id: 2 }] 
    };
    userSpy.getSubscriptions.and.returnValue(of(mockSubs));
    newsSpy.filterNews.and.returnValue(of({ content: [{ id: 100, title: 'Subbed News' }], totalPages: 1 }));

    service.loadNews();

    expect(userSpy.getSubscriptions).toHaveBeenCalled();
    expect(newsSpy.filterNews).toHaveBeenCalledWith([1], [2], 0);
    expect(service.newsItems().length).toBe(1);
  });
});
