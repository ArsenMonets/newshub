import { TestBed } from '@angular/core/testing';
import { SubscriptionsService } from './subscriptions.service';
import { UserAPI } from '../api/user.api';
import { of } from 'rxjs';

describe('SubscriptionsService', () => {
  let service: SubscriptionsService;
  let userSpy: any;

  beforeEach(() => {
    userSpy = jasmine.createSpyObj('UserAPI', ['getSubscriptions', 'unsubscribeFromCategory', 'unsubscribeFromAuthor']);
    userSpy.getSubscriptions.and.returnValue(of({ categories: [], authors: [] }));

    TestBed.configureTestingModule({
      providers: [
        SubscriptionsService,
        { provide: UserAPI, useValue: userSpy }
      ]
    });
    service = TestBed.inject(SubscriptionsService);
  });

  it('should load subs on init', () => {
    const data = { categories: [{ id: 1 }], authors: [] };
    userSpy.getSubscriptions.and.returnValue(of(data));
    service.loadSubs();
    expect(service.subs()).toEqual(data as any);
  });

  it('should unsubscribe from category', () => {
    userSpy.unsubscribeFromCategory.and.returnValue(of({}));
    service.unsubCat(1);
    expect(userSpy.unsubscribeFromCategory).toHaveBeenCalledWith(1);
  });

  it('should unsubscribe from author', () => {
    userSpy.unsubscribeFromAuthor.and.returnValue(of({}));
    service.unsubAuthor(2);
    expect(userSpy.unsubscribeFromAuthor).toHaveBeenCalledWith(2);
  });

  it('should call unsubscribeFromAuthor when unsubAuthor is called', () => {
    userSpy.unsubscribeFromAuthor.and.returnValue(of({ categories: [], authors: [] }));
    service.unsubAuthor(99);
    expect(userSpy.unsubscribeFromAuthor).toHaveBeenCalledWith(99);
  });

  it('should load subscriptions on loadSubs call', () => {
    service.loadSubs();
    expect(userSpy.getSubscriptions).toHaveBeenCalled();
  });
});
