import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminAPI } from '../api/admin.api';
import { NewsAPI } from '../api/news.api';
import { CategoryDTO, UserDTO, Page } from '../models/models';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: '../components/admin.component.html',
  styleUrls: ['../../static/admin.component.css']
})
export class AdminService {
  private readonly adminAPI = inject(AdminAPI);
  private readonly newsAPI = inject(NewsAPI);

  categories = signal<CategoryDTO[]>([]);
  users = signal<UserDTO[]>([]);
  usersPage = signal<Page<UserDTO> | null>(null);
  currentPage = signal<number>(0);
  loginFilter = signal<string>('');
  pageSize = signal<number>(10);
  editingCategoryId = signal<number | null>(null);
  editingCategoryName = signal<string>('');

  constructor() {
    this.loadData();
  }

  loadData() {
    this.newsAPI.getCategories().subscribe((c: CategoryDTO[]) => this.categories.set(c));
    this.loadNonAdminUsers();
  }

  loadNonAdminUsers() {
    this.adminAPI.getAllNonAdminUsers(this.loginFilter(), this.currentPage(), this.pageSize())
      .subscribe((page: Page<UserDTO>) => {
        this.usersPage.set(page);
        this.users.set(page.content);
      });
  }

  onLoginFilterChange(filter: string) {
    this.loginFilter.set(filter);
    this.currentPage.set(0);
    this.loadNonAdminUsers();
  }

  goToPage(page: number) {
    this.currentPage.set(page);
    this.loadNonAdminUsers();
  }

  addCategory(name: string) {
    if (!name) return;
    this.adminAPI.addCategory(name).subscribe(() => this.loadData());
  }

  startEditCategory(cat: CategoryDTO) {
    this.editingCategoryId.set(cat.id);
    this.editingCategoryName.set(cat.name);
  }

  cancelEditCategory() {
    this.editingCategoryId.set(null);
    this.editingCategoryName.set('');
  }

  saveEditCategory(id: number, newName: string) {
    if (!newName) return;
    this.adminAPI.updateCategory(id, newName).subscribe(() => {
      this.cancelEditCategory();
      this.loadData();
    });
  }

  deleteCategory(id: number) {
    this.adminAPI.deleteCategory(id).subscribe({
      next: () => this.loadData(),
      error: (err) => {}
    });
  }

  toggleBlock(user: UserDTO, block: boolean) {
    const action = block ? this.adminAPI.blockUser(user.id) : this.adminAPI.unblockUser(user.id);
    action.subscribe((updated: UserDTO) => {
      this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
    });
  }

  makeAuthor(user: UserDTO) {
    this.adminAPI.changeUserRole(user.id, 'AUTHOR').subscribe((updated: UserDTO) => {
      this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
    });
  }

  returnUserToUser(user: UserDTO) {
    this.adminAPI.changeUserRole(user.id, 'USER').subscribe((updated: UserDTO) => {
      this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
    });
  }
}
