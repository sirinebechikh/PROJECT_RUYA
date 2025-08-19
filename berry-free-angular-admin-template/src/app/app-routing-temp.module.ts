import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

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
    path: 'login',
    loadComponent: () => import('./demo/pages/authentication/login/login.component').then(m => m.default)
  },
  {
    path: '**',
    redirectTo: '/default'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }