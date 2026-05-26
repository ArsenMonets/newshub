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
  private userAPI = inject(UserAPI);
  subs = signal<SubscriptionsDTO | null>(null);

  ngOnInit() {
    this.loadSubs();
  }

  loadSubs() {
    this.userAPI.getSubscriptions().subscribe((data: SubscriptionsDTO) => this.subs.set(data));
  }

  unsubCat(id: number) {
    this.userAPI.unsubscribeFromCategory(id).subscribe((data: SubscriptionsDTO) => this.subs.set(data));
  }

  unsubAuthor(id: number) {
    this.userAPI.unsubscribeFromAuthor(id).subscribe((data: SubscriptionsDTO) => this.subs.set(data));
  }
}
