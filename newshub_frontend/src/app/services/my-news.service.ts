import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NewsAPI } from '../api/news.api';
import { AuthorAPI } from '../api/author.api';
import { AuthAPI } from '../api/auth.api';
import { NewsDTO } from '../models/models';

@Component({
  selector: 'app-my-news',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: '../components/my-news.component.html',
  styleUrls: ['../../static/my-news.component.css']
})
export class MyNewsService implements OnInit {
  private readonly newsAPI = inject(NewsAPI);
  private readonly authorAPI = inject(AuthorAPI);
  private readonly authAPI = inject(AuthAPI);
  private readonly router = inject(Router);

  myNews = signal<NewsDTO[]>([]);
  isLoading = signal(false);

  ngOnInit() {
    this.loadMyNews();
  }

  loadMyNews() {
    this.isLoading.set(true);
    const currentUserId = this.authAPI.user()!.id;
    this.newsAPI.filterNews([], [currentUserId]).subscribe({
      next: (page: any) => {
        this.myNews.set(page.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  editNews(news: NewsDTO) {
    this.newsAPI.getNewsDetails(news.id).subscribe({
      next: (fullNews: NewsDTO) => {
        this.router.navigate(['/create-news'], { state: { news: fullNews } });
      }
    });
  }

  deleteNews(news: NewsDTO) {
    if (!confirm(`Delete "${news.title}"?`)) return;

    this.authorAPI.deleteNews(news.id).subscribe({
      next: () => {
        this.myNews.update(list => list.filter(n => n.id !== news.id));
      }
    });
  }
}
