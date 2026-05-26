import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ErrorAPI } from '../api/error.api';
import { AuthAPI } from '../api/auth.api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: '../components/app.component.html',
  styleUrls: ['../../static/app.component.css']
})
export class AppService {
  errorAPI = inject(ErrorAPI);
  authAPI = inject(AuthAPI);
}
