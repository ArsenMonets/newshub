import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ErrorAPI {
  private readonly errorSignal = signal<string | null>(null);
  
  error = this.errorSignal.asReadonly();

  setError(message: string) {
    this.errorSignal.set(message);
    setTimeout(() => this.clearError(), 5000);
  }

  clearError() {
    this.errorSignal.set(null);
  }
}
