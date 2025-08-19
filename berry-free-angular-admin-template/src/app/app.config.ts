import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

// Configuration simplifiée pour Angular 17
const routes = [
  {
    path: '',
    redirectTo: '/default',
    pathMatch: 'full' as const
  },
  {
    path: 'default',
    loadComponent: () => import('./demo/dashboard/default/default.component').then(m => m.DefaultComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./demo/pages/authentication/login/login.component').then(m => m.default)
  },
  {
    path: '**',
    redirectTo: '/default'
  }
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations()
  ]
};