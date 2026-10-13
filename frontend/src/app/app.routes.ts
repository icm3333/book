import { Routes } from '@angular/router';

import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Sign in · Book Tracker',
    loadComponent: () => import('./features/auth/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    title: 'Create account · Book Tracker',
    loadComponent: () => import('./features/auth/register-page').then((m) => m.RegisterPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/app-shell').then((m) => m.AppShell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'library' },
      {
        path: 'library',
        title: 'My library · Book Tracker',
        loadComponent: () => import('./features/library/library-page').then((m) => m.LibraryPage),
      },
      {
        path: 'search',
        title: 'Find books · Book Tracker',
        loadComponent: () => import('./features/search/search-page').then((m) => m.SearchPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
