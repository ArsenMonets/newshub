import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NewsDTO, NewsInputDTO } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthorAPI {
  private http = inject(HttpClient);
  private baseUrl = '/api/v1/author';

  createNews(data: NewsInputDTO) {
    return this.http.post<NewsDTO>(`${this.baseUrl}/news`, data);
  }

  updateNews(id: number, data: NewsInputDTO) {
    return this.http.put<NewsDTO>(`${this.baseUrl}/news/${id}`, data);
  }

  deleteNews(id: number) {
    return this.http.delete(`${this.baseUrl}/news/${id}`, { responseType: 'text' });
  }
}
