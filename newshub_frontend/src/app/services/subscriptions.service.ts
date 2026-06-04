import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserAPI } from '../api/user.api';
import { SubscriptionsDTO } from '../models/models';

@Component({
  selector: 'app-subscriptions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: '../components/subscriptions.component.html',
  styleUrls: ['../../static/subscriptions.component.css']
})
export class SubscriptionsService implements OnInit {
  private readonly userAPI = inject(UserAPI);
  subs = signal<SubscriptionsDTO | null>(null);

  ngOnInit() {
    this.loadSubs();
  }

  loadSubs() {
    this.userAPI.getSubscriptions().subscribe({
      next: (data: SubscriptionsDTO) => this.subs.set(data),
      error: (err) => {}
    });
  }

  unsubCat(id: number) {
    this.userAPI.unsubscribeFromCategory(id).subscribe({
      next: (data: SubscriptionsDTO) => this.subs.set(data),
      error: (err) => {}
    });
  }

  unsubAuthor(id: number) {
    this.userAPI.unsubscribeFromAuthor(id).subscribe({
      next: (data: SubscriptionsDTO) => this.subs.set(data),
      error: (err) => {}
    });
  }
}
