import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SubscriptionsDTO } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class UserAPI {
  private http = inject(HttpClient);
  private baseUrl = '/api/v1/user';

  subscribeToCategory(categoryId: number) {
    return this.http.post<SubscriptionsDTO>(`${this.baseUrl}/subscribe/category/${categoryId}`, {});
  }

  unsubscribeFromCategory(categoryId: number) {
    return this.http.delete<SubscriptionsDTO>(`${this.baseUrl}/unsubscribe/category/${categoryId}`);
  }

  subscribeToAuthor(authorId: number) {
    return this.http.post<SubscriptionsDTO>(`${this.baseUrl}/subscribe/author/${authorId}`, {});
  }

  unsubscribeFromAuthor(authorId: number) {
    return this.http.delete<SubscriptionsDTO>(`${this.baseUrl}/unsubscribe/author/${authorId}`);
  }

  getSubscriptions() {
    return this.http.get<SubscriptionsDTO>(`${this.baseUrl}/subscriptions`);
  }
}
