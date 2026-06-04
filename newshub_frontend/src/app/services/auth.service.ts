import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthAPI } from '../api/auth.api';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: '../components/login.component.html',
  styleUrls: ['../../static/login.component.css']
})
export class LoginService {
  private readonly authAPI = inject(AuthAPI);
  private readonly router = inject(Router);

  loginData = { login: '', password: '' };
  loading = signal(false);

  onLogin() {
    this.loading.set(true);
    this.authAPI.login(this.loginData).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: () => this.loading.set(false)
    });
  }
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: '../components/register.component.html',
  styleUrls: ['../../static/register.component.css']
})
export class RegisterService {
  private readonly authAPI = inject(AuthAPI);
  private readonly router = inject(Router);

  regData = { login: '', email: '', password: '' };
  loading = signal(false);

  onRegister() {
    this.loading.set(true);
    this.authAPI.register(this.regData).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: () => this.loading.set(false)
    });
  }
}
