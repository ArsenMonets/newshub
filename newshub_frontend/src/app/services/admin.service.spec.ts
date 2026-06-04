import { TestBed } from '@angular/core/testing';
import { AdminService } from './admin.service';
import { AdminAPI } from '../api/admin.api';
import { NewsAPI } from '../api/news.api';
import { of, throwError } from 'rxjs';

describe('AdminService', () => {
  let service: AdminService;
  let adminSpy: any;
  let newsSpy: any;

  beforeEach(() => {
    adminSpy = jasmine.createSpyObj('AdminAPI', ['getAllNonAdminUsers', 'addCategory', 'updateCategory', 'deleteCategory', 'blockUser', 'unblockUser', 'changeUserRole']);
    newsSpy = jasmine.createSpyObj('NewsAPI', ['getCategories']);

    newsSpy.getCategories.and.returnValue(of([]));
    adminSpy.getAllNonAdminUsers.and.returnValue(of({ content: [], totalElements: 0 }));

    TestBed.configureTestingModule({
      providers: [
        AdminService,
        { provide: AdminAPI, useValue: adminSpy },
        { provide: NewsAPI, useValue: newsSpy }
      ]
    });
    service = TestBed.inject(AdminService);
  });

  it('should load categories on init', () => {
    const cats = [{ id: 1, name: 'Test' }];
    newsSpy.getCategories.and.returnValue(of(cats));
    service.loadData();
    expect(service.categories()).toEqual(cats);
  });

  it('should load users', () => {
    const page = { content: [{ id: 1, login: 'u1', role: 'USER', isBlocked: false } as any], totalElements: 1 };
    adminSpy.getAllNonAdminUsers.and.returnValue(of(page));
    service.loadNonAdminUsers();
    expect(service.users()).toEqual(page.content);
  });

  it('should filter users', () => {
    service.onLoginFilterChange('abc');
    expect(service.loginFilter()).toBe('abc');
    expect(adminSpy.getAllNonAdminUsers).toHaveBeenCalledWith('abc', 0, 10);
  });

  it('should go to page', () => {
    service.goToPage(2);
    expect(service.currentPage()).toBe(2);
    expect(adminSpy.getAllNonAdminUsers).toHaveBeenCalled();
  });

  it('should add category', () => {
    adminSpy.addCategory.and.returnValue(of({}));
    service.addCategory('New');
    expect(adminSpy.addCategory).toHaveBeenCalledWith('New');
  });

  it('should start editing category', () => {
    const cat = { id: 5, name: 'Old' };
    service.startEditCategory(cat);
    expect(service.editingCategoryId()).toBe(5);
    expect(service.editingCategoryName()).toBe('Old');
  });

  it('should cancel editing', () => {
    service.cancelEditCategory();
    expect(service.editingCategoryId()).toBeNull();
  });

  it('should save edited category', () => {
    adminSpy.updateCategory.and.returnValue(of({}));
    service.saveEditCategory(1, 'Updated');
    expect(adminSpy.updateCategory).toHaveBeenCalledWith(1, 'Updated');
  });

  it('should delete category', () => {
    adminSpy.deleteCategory.and.returnValue(of({}));
    service.deleteCategory(1);
    expect(adminSpy.deleteCategory).toHaveBeenCalledWith(1);
  });

  it('should toggle block user', () => {
    const user = { id: 1, login: 'u', role: 'USER', isBlocked: false } as any;
    adminSpy.blockUser.and.returnValue(of({ ...user, isBlocked: true }));
    service.toggleBlock(user, true);
    expect(adminSpy.blockUser).toHaveBeenCalledWith(1);
  });

  it('should make author', () => {
    const user = { id: 1, login: 'u', role: 'USER' } as any;
    adminSpy.changeUserRole.and.returnValue(of({ ...user, role: 'AUTHOR' }));
    service.makeAuthor(user);
    expect(adminSpy.changeUserRole).toHaveBeenCalledWith(1, 'AUTHOR');
  });

  it('should return user to user role', () => {
    const user = { id: 1, login: 'u', role: 'AUTHOR' } as any;
    adminSpy.changeUserRole.and.returnValue(of({ ...user, role: 'USER' }));
    service.returnUserToUser(user);
    expect(adminSpy.changeUserRole).toHaveBeenCalledWith(1, 'USER');
  });

  it('should not add category if name is empty', () => {
    service.addCategory('');
    expect(adminSpy.addCategory).not.toHaveBeenCalled();
  });

  it('should not save category if name is empty', () => {
    service.saveEditCategory(1, '');
    expect(adminSpy.updateCategory).not.toHaveBeenCalled();
  });

  it('should load non admin users with current filters', () => {
    service.loginFilter.set('test');
    service.currentPage.set(5);
    service.loadNonAdminUsers();
    expect(adminSpy.getAllNonAdminUsers).toHaveBeenCalledWith('test', 5, 10);
  });

  it('should not delete category if handle error', () => {
    adminSpy.deleteCategory.and.returnValue(throwError(() => 'err'));
    service.deleteCategory(1);
    expect(adminSpy.deleteCategory).toHaveBeenCalled();
  });

  it('should initialize with default pagination values', () => {
    expect(service.currentPage()).toBe(0);
    expect(service.pageSize()).toBe(10);
  });

  it('should update user list after blocking', () => {
    const user = { id: 1, login: 'u', isBlocked: false } as any;
    service.users.set([user]);
    adminSpy.blockUser.and.returnValue(of({ ...user, isBlocked: true }));
    service.toggleBlock(user, true);
    expect(service.users()[0].isBlocked).toBeTrue();
  });

  it('should update user role to USER and update list', () => {
    const user = { id: 1, role: 'AUTHOR' } as any;
    service.users.set([user]);
    adminSpy.changeUserRole.and.returnValue(of({ ...user, role: 'USER' }));
    service.returnUserToUser(user);
    expect(service.users()[0].role).toBe('USER');
  });
});
