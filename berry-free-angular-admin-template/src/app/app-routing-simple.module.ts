import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Routes simplifiées pour Angular 17
const routes: Routes = [
  {
    path: '',
    redirectTo: '/default',
    pathMatch: 'full'
  },
  {
    path: 'default',
    loadComponent: () => import('./demo/dashboard/default/default.component').then(m => m.DefaultComponent)
  },
  {
    path: 'guest',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./demo/pages/authentication/login/login.component').then(m => m.default)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/default'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    enableTracing: false,
    scrollPositionRestoration: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }