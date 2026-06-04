import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CategoryDTO, UserDTO, Page } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AdminAPI {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/admin';

  blockUser(userId: number) {
    return this.http.post<UserDTO>(`${this.baseUrl}/users/${userId}/block`, {});
  }

  unblockUser(userId: number) {
    return this.http.post<UserDTO>(`${this.baseUrl}/users/${userId}/unblock`, {});
  }

  changeUserRole(userId: number, newRole: string) {
    return this.http.put<UserDTO>(`${this.baseUrl}/users/${userId}/role`, {}, {
      params: { newRole }
    });
  }

  addCategory(name: string) {
    return this.http.post<CategoryDTO>(`${this.baseUrl}/categories`, {}, {
      params: { name }
    });
  }

  updateCategory(id: number, name: string) {
    return this.http.put<CategoryDTO>(`${this.baseUrl}/categories/${id}`, {}, {
      params: { name }
    });
  }

  deleteCategory(id: number) {
    return this.http.delete(`${this.baseUrl}/categories/${id}`, { responseType: 'text' });
  }

  getAllNonAdminUsers(loginFilter: string = '', page: number = 0, size: number = 10) {
    let params = new HttpParams()
      .set('loginFilter', loginFilter)
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<UserDTO>>(`${this.baseUrl}/users`, { params });
  }
}
