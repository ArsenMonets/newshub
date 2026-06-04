import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { CategoryDTO, NewsDTO, NewsPreviewDTO, Page, UserDTO } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class NewsAPI {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/public';

  searchNews(query: string, page: number = 0) {
    return this.http.get<Page<NewsPreviewDTO>>(`${this.baseUrl}/news/search`, {
      params: { query, page: page.toString() }
    });
  }

  filterNews(categoryIds: number[], authorIds: number[], page: number = 0) {
    let params = new HttpParams().set('page', page.toString());
    categoryIds.forEach(id => params = params.append('categoryIds', id.toString()));
    authorIds.forEach(id => params = params.append('authorIds', id.toString()));
    return this.http.get<Page<NewsPreviewDTO>>(`${this.baseUrl}/news/filter`, { params });
  }

  getCategories() {
    return this.http.get<CategoryDTO[]>(`${this.baseUrl}/categories`);
  }

  getAuthors() {
    return this.http.get<UserDTO[]>(`${this.baseUrl}/authors`);
  }

  getNewsDetails(id: number) {
    return this.http.get<NewsDTO>(`${this.baseUrl}/news/${id}`);
  }
}
