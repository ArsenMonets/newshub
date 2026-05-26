import { Routes } from '@angular/router';
import { HomeService } from './services/home.service';
import { NewsDetailService } from './services/news-detail.service';
import { LoginService, RegisterService } from './services/auth.service';
import { SubscriptionsService } from './services/subscriptions.service';
import { CreateNewsService } from './services/create-news.service.';
import { AdminService } from './services/admin.service';
import { MyNewsService } from './services/my-news.service';

export const routes: Routes = [
  { path: '', component: HomeService },
  { path: 'news/:id', component: NewsDetailService },
  { path: 'login', component: LoginService },
  { path: 'register', component: RegisterService },
  { path: 'subscriptions', component: SubscriptionsService },
  { path: 'create-news', component: CreateNewsService },
  { path: 'my-news', component: MyNewsService },
  { path: 'admin', component: AdminService },
  { path: '**', redirectTo: '' }
];
