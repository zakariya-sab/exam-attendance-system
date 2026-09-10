import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { AuthService } from '../services/auth.service';

const LOGIN_URL = 'http://localhost:8080/api/auth/login';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url === LOGIN_URL) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = authService.getToken();

  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};
