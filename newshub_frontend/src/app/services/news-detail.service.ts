import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NewsAPI } from '../api/news.api';
import { UserAPI } from '../api/user.api';
import { AuthorAPI } from '../api/author.api';
import { NewsDTO } from '../models/models';
import { AuthAPI } from '../api/auth.api';

@Component({
  selector: 'app-news-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: '../components/news-detail.component.html',
  styleUrls: ['../../static/news-detail.component.css']
})
export class NewsDetailService implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly newsAPI = inject(NewsAPI);
  private readonly userAPI = inject(UserAPI);
  private readonly authorAPI = inject(AuthorAPI);
  readonly authAPI = inject(AuthAPI);

  news = signal<NewsDTO | null>(null);

  ngOnInit() {
    const id = this.route.snapshot.params['id'];
    this.newsAPI.getNewsDetails(id).subscribe((data: NewsDTO) => this.news.set(data));
  }

  canEditOrDelete(): boolean {
    const currentUser = this.authAPI.user();
    const newsAuthor = this.news()?.author;
    
    if (!currentUser || !newsAuthor) return false;
    
    // Адміни можуть редагувати/видаляти всі пости
    if (currentUser.role === 'ADMIN') return true;
    
    // Автори можуть редагувати/видаляти тільки свої пости
    return currentUser.id === newsAuthor.id;
  }

  deleteNews() {
    if (!this.news() || !confirm('Are you sure you want to delete this news?')) return;
    
    this.authorAPI.deleteNews(this.news()!.id).subscribe(() => {
      this.router.navigate(['/']);
    });
  }

  editNews() {
    if (!this.news()) return;
    this.router.navigate(['/create-news'], { state: { news: this.news() } });
  }

  subscribeToAuthor(id: number) {
    this.userAPI.subscribeToAuthor(id).subscribe(() => alert('Subscribed!'));
  }

  subscribeToCategory(id: number) {
    this.userAPI.subscribeToCategory(id).subscribe(() => alert('Subscribed!'));
  }
}
