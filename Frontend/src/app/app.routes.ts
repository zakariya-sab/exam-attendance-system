import { Routes } from '@angular/router';

import { Login } from './auth/login/login';
import { ExamBlockList } from './exam-blocks/exam-block-list/exam-block-list';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'exam-blocks', component: ExamBlockList, canActivate: [authGuard] },
  { path: '', redirectTo: 'exam-blocks', pathMatch: 'full' },
];
