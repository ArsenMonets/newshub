import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ErrorAPI {
  private errorSignal = signal<string | null>(null);
  
  error = this.errorSignal.asReadonly();

  setError(message: string) {
    this.errorSignal.set(message);
    // Auto-clear after 5 seconds
    setTimeout(() => this.clearError(), 5000);
  }

  clearError() {
    this.errorSignal.set(null);
  }
}
