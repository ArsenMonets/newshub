import { TestBed } from '@angular/core/testing';
import { NewsDetailService } from './news-detail.service';
import { NewsAPI } from '../api/news.api';
import { UserAPI } from '../api/user.api';
import { AuthorAPI } from '../api/author.api';
import { AuthAPI } from '../api/auth.api';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('NewsDetailService', () => {
  let service: NewsDetailService;
  let newsSpy: any;
  let userSpy: any;
  let authorSpy: any;
  let authSpy: any;
  let routerSpy: any;

  beforeEach(() => {
    newsSpy = jasmine.createSpyObj('NewsAPI', ['getNewsDetails']);
    userSpy = jasmine.createSpyObj('UserAPI', ['subscribeToAuthor', 'subscribeToCategory']);
    authorSpy = jasmine.createSpyObj('AuthorAPI', ['deleteNews']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    
    authSpy = { user: () => ({ id: 1, role: 'USER' }) };

    TestBed.configureTestingModule({
      providers: [
        NewsDetailService,
        { provide: NewsAPI, useValue: newsSpy },
        { provide: UserAPI, useValue: userSpy },
        { provide: AuthorAPI, useValue: authorSpy },
        { provide: AuthAPI, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { params: { id: 123 } } }
        }
      ]
    });

    newsSpy.getNewsDetails.and.returnValue(of({ id: 123, author: { id: 1 } }));
    service = TestBed.inject(NewsDetailService);
  });

  it('should load news details on init', () => {
    service.ngOnInit();
    expect(newsSpy.getNewsDetails).toHaveBeenCalledWith(123);
    expect(service.news()?.id).toBe(123);
  });

  it('should check if user can edit own news', () => {
    service.news.set({ id: 123, author: { id: 1 } } as any);
    expect(service.canEditOrDelete()).toBeTrue();
  });

  it('should allow admin to edit any news', () => {
    // @ts-ignore
    authSpy.user = () => ({ id: 2, role: 'ADMIN' });
    service.news.set({ id: 123, author: { id: 1 } } as any);
    expect(service.canEditOrDelete()).toBeTrue();
  });

  it('should not allow other users to edit', () => {
    // @ts-ignore
    authSpy.user = () => ({ id: 2, role: 'USER' });
    service.news.set({ id: 123, author: { id: 1 } } as any);
    expect(service.canEditOrDelete()).toBeFalse();
  });

  it('should delete news', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    service.news.set({ id: 123 } as any);
    authorSpy.deleteNews.and.returnValue(of({}));
    service.deleteNews();
    expect(authorSpy.deleteNews).toHaveBeenCalledWith(123);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should navigate to edit', () => {
    const news = { id: 123 } as any;
    service.news.set(news);
    service.editNews();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/create-news'], { state: { news } });
  });

  it('should subscribe to author', () => {
    userSpy.subscribeToAuthor.and.returnValue(of({}));
    spyOn(window, 'alert');
    service.subscribeToAuthor(1);
    expect(userSpy.subscribeToAuthor).toHaveBeenCalledWith(1);
  });

  it('should alert after subscribing to category', () => {
    userSpy.subscribeToCategory.and.returnValue(of({}));
    spyOn(window, 'alert');
    service.subscribeToCategory(10);
    expect(userSpy.subscribeToCategory).toHaveBeenCalledWith(10);
  });

  it('should not allow non-logged user to edit', () => {
    // @ts-ignore
    authSpy.user = () => null;
    expect(service.canEditOrDelete()).toBeFalse();
  });
});
