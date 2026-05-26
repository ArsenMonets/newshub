import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NewsAPI } from '../api/news.api';
import { UserAPI } from '../api/user.api';
import { AuthAPI } from '../api/auth.api';
import { WebSocketAPI } from '../api/websocket.api';
import { NewsPreviewDTO, CategoryDTO, UserDTO } from '../models/models';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: '../components/home.component.html',
  styleUrls: ['../../static/home.component.css']
})
export class HomeService {
  private newsAPI = inject(NewsAPI);
  private userAPI = inject(UserAPI);
  private wsAPI = inject(WebSocketAPI);
  authService = inject(AuthAPI);

  newsItems = signal<NewsPreviewDTO[]>([]);
  categories = signal<CategoryDTO[]>([]);
  authors = signal<UserDTO[]>([]);
  subscribedCategoryIds = signal<number[]>([]);
  subscribedAuthorIds = signal<number[]>([]);
  
  searchQuery = '';
  selectedCategories: number[] = [];
  selectedAuthors: number[] = [];
  page = signal(0);
  totalPages = signal(0);
  showAll = signal(false);

  constructor() {
    this.loadInitialData();
    this.setupWebSockets();
  }

  loadInitialData() {
    this.newsAPI.getCategories().subscribe((c: CategoryDTO[]) => this.categories.set(c));
    this.newsAPI.getAuthors().subscribe((a: UserDTO[]) => this.authors.set(a));
    
    // Завантажуємо підписки якщо користувач авторизований
    if (this.authService.user()) {
      this.loadUserSubscriptions();
    }
    
    this.loadNews();
  }

  loadUserSubscriptions() {
    this.userAPI.getSubscriptions().subscribe((subscriptions: any) => {
      this.subscribedCategoryIds.set(subscriptions.categories.map((c: any) => c.id));
      this.subscribedAuthorIds.set(subscriptions.authors.map((a: any) => a.id));
    });
  }

  isSubscribedToCategory(categoryId: number): boolean {
    return this.subscribedCategoryIds().includes(categoryId);
  }

  isSubscribedToAuthor(authorId: number): boolean {
    return this.subscribedAuthorIds().includes(authorId);
  }

  loadNews() {
    if (this.searchQuery) {
      this.newsAPI.searchNews(this.searchQuery, this.page()).subscribe((res: any) => {
        this.newsItems.set(res.content);
        this.totalPages.set(res.totalPages);
      });
    } else if (this.showAll() || !this.authService.user()) {
      this.newsAPI.filterNews(this.selectedCategories, this.selectedAuthors, this.page()).subscribe((res: any) => {
        this.newsItems.set(res.content);
        this.totalPages.set(res.totalPages);
      });
    } else {
      this.userAPI.getSubscriptions().subscribe({
        next: (subscriptions: any) => {
          const categoryIds = subscriptions.categories.map((c: any) => c.id);
          const authorIds = subscriptions.authors.map((a: any) => a.id);
          
          if (categoryIds.length === 0 && authorIds.length === 0) {
            this.newsItems.set([]);
            this.totalPages.set(0);
            return;
          }
          
          this.newsAPI.filterNews(categoryIds, authorIds, this.page()).subscribe((res: any) => {
            this.newsItems.set(res.content);
            this.totalPages.set(res.totalPages);
          });
        },
        error: (err) => {
          console.log('User not authenticated, showing empty news');
          this.newsItems.set([]);
          this.totalPages.set(0);
        }
      });
    }
  }

  onSearch() {
    this.page.set(0);
    this.loadNews();
  }

  toggleShowAll() {
    this.showAll.update(val => !val);
    this.page.set(0);
    this.selectedCategories = [];
    this.selectedAuthors = [];
    this.searchQuery = '';
    this.loadNews();
  }

  toggleCategory(id: number) {
    const idx = this.selectedCategories.indexOf(id);
    if (idx > -1) this.selectedCategories.splice(idx, 1);
    else this.selectedCategories.push(id);
    this.page.set(0);
    this.showAll.set(true);
    this.loadNews();
  }

  toggleAuthor(id: number) {
    const idx = this.selectedAuthors.indexOf(id);
    if (idx > -1) this.selectedAuthors.splice(idx, 1);
    else this.selectedAuthors.push(id);
    this.page.set(0);
    this.showAll.set(true);
    this.loadNews();
  }

  prevPage() {
    if (this.page() > 0) {
      this.page.update(p => p - 1);
      this.loadNews();
    }
  }

  nextPage() {
    if (this.page() < this.totalPages() - 1) {
      this.page.update(p => p + 1);
      this.loadNews();
    }
  }

  subscribeToCategory(categoryId: number) {
    this.userAPI.subscribeToCategory(categoryId).subscribe(() => {
      this.subscribedCategoryIds.update(ids => [...ids, categoryId]);
    });
  }

  unsubscribeFromCategory(categoryId: number) {
    this.userAPI.unsubscribeFromCategory(categoryId).subscribe(() => {
      this.subscribedCategoryIds.update(ids => ids.filter(id => id !== categoryId));
    });
  }

  subscribeToAuthor(authorId: number) {
    this.userAPI.subscribeToAuthor(authorId).subscribe(() => {
      this.subscribedAuthorIds.update(ids => [...ids, authorId]);
    });
  }

  unsubscribeFromAuthor(authorId: number) {
    this.userAPI.unsubscribeFromAuthor(authorId).subscribe(() => {
      this.subscribedAuthorIds.update(ids => ids.filter(id => id !== authorId));
    });
  }

  private setupWebSockets() {
    this.wsAPI.newsCreated$.subscribe(news => {
      // Logic: Only add if current query/filter might include it (simplified: always add if no active filters)
      if (!this.searchQuery && this.page() === 0) {
        this.newsItems.update(items => [news, ...items].slice(0, 10));
      }
    });

    this.wsAPI.newsUpdated$.subscribe(updated => {
      this.newsItems.update(items => items.map(item => item.id === updated.id ? updated : item));
    });

    this.wsAPI.newsDeleted$.subscribe(id => {
      this.newsItems.update(items => items.filter(item => item.id !== id));
    });
  }
}
