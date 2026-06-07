import { TestBed } from '@angular/core/testing';
import { MyNewsService } from './my-news.service';
import { NewsAPI } from '../api/news.api';
import { AuthorAPI } from '../api/author.api';
import { AuthAPI } from '../api/auth.api';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('MyNewsService', () => {
  let service: MyNewsService;
  let newsSpy: any;
  let authorSpy: any;
  let authSpy: any;
  let routerSpy: any;

  beforeEach(() => {
    newsSpy = jasmine.createSpyObj('NewsAPI', ['filterNews', 'getNewsDetails']);
    authorSpy = jasmine.createSpyObj('AuthorAPI', ['deleteNews']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    authSpy = { user: () => ({ id: 1 }) };

    newsSpy.filterNews.and.returnValue(of({ content: [] }));

    TestBed.configureTestingModule({
      providers: [
        MyNewsService,
        { provide: NewsAPI, useValue: newsSpy },
        { provide: AuthorAPI, useValue: authorSpy },
        { provide: AuthAPI, useValue: authSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
    service = TestBed.inject(MyNewsService);
  });

  it('should load my news on init', () => {
    const list = [{ id: 1, title: 'T' }];
    newsSpy.filterNews.and.returnValue(of({ content: list }));
    service.ngOnInit();
    expect(service.myNews()).toEqual(list as any);
  });

  it('should navigate to edit news', () => {
    const news = { id: 1 } as any;
    newsSpy.getNewsDetails.and.returnValue(of(news));
    service.editNews(news);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/create-news'], { state: { news } });
  });

  it('should delete news after confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    const news = { id: 1, title: 'T' } as any;
    authorSpy.deleteNews.and.returnValue(of({}));
    service.deleteNews(news);
    expect(authorSpy.deleteNews).toHaveBeenCalledWith(1);
  });

  it('should not delete news if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    service.deleteNews({ id: 1 } as any);
    expect(authorSpy.deleteNews).not.toHaveBeenCalled();
  });

  it('should reload news on loadMyNews call', () => {
    service.loadMyNews();
    expect(newsSpy.filterNews).toHaveBeenCalled();
  });

  it('should set isLoading while loading news', () => {
    newsSpy.filterNews.and.returnValue(of({ content: [] }));
    service.loadMyNews();
    expect(service.isLoading()).toBeFalse();
  });
});
