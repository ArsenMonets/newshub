import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ErrorAPI } from '../api/error.api';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private errorService = inject(ErrorAPI);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: any) => {
        let errorMessage = 'An unknown error occurred!';
        
        if (error.error instanceof ErrorEvent) {
          errorMessage = `Client-side error: ${error.error.message}`;
        } else if (typeof error.error === 'string') {
          // Спробуємо парсити як JSON
          try {
            const parsed = JSON.parse(error.error);
            if (parsed.message) {
              errorMessage = parsed.message;
            } else {
              errorMessage = error.error;
            }
          } catch (e) {
            if (error.error.startsWith('<html') || error.error.startsWith('<!DOCTYPE')) {
              const method = req.method;
              const url = req.url.split('/').pop() || req.url;
              errorMessage = `Cannot ${method} ${url} (${error.status})`;
            } else {
              errorMessage = error.error;
            }
          }
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (typeof error.error === 'object' && error.error !== null) {
          errorMessage = JSON.stringify(error.error);
        } else {
          errorMessage = `Server error: ${error.status}`;
        }
        
        this.errorService.setError(errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    if (token) {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
      return next.handle(cloned);
    }
    return next.handle(req);
  }
}
