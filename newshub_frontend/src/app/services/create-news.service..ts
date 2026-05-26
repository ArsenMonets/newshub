import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthorAPI } from '../api/author.api';
import { NewsAPI } from '../api/news.api';
import { Router } from '@angular/router';
import { CategoryDTO, NewsDTO } from '../models/models';

@Component({
  selector: 'app-create-news',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: '../components/create-news.component.html',
  styleUrls: ['../../static/create-news.component.css']
})
export class CreateNewsService {
  private authorAPI = inject(AuthorAPI);
  private newsAPI = inject(NewsAPI);
  private router = inject(Router);

  newsInput = { title: '', content: '', categoryId: 0 };
  categories = signal<CategoryDTO[]>([]);
  editingNewsId = signal<number | null>(null);

  constructor() {
    this.newsAPI.getCategories().subscribe((c: CategoryDTO[]) => this.categories.set(c));
    
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state?.['news']) {
      const news: NewsDTO = navigation.extras.state['news'];
      this.editingNewsId.set(news.id);
      this.newsInput = {
        title: news.title,
        content: news.content,
        categoryId: news.category.id
      };
    }
  }

  onCreate() {
    if (this.editingNewsId()) {
      // Редагування існуючого посту
      this.authorAPI.updateNews(this.editingNewsId()!, this.newsInput).subscribe(() => {
        this.router.navigate(['/']);
      });
    } else {
      // Створення нового посту
      this.authorAPI.createNews(this.newsInput).subscribe(() => {
        this.router.navigate(['/']);
      });
    }
  }
}
