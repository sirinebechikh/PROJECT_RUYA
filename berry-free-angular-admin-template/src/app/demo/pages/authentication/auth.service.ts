 import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Utilisateur } from '../gestion-utilisateur/gestion-utilisateur.component';

export interface User {
  id?: number;
  username: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    email: string;
    username: string;
  };
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/users';

  constructor(private http: HttpClient) {}

  // ✅ Méthode d'inscription corrigée
  register(user: User): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.post(`${this.apiUrl}`, user, { headers })
      .pipe(
        tap(response => console.log('✅ Inscription réussie:', response)),
        catchError(this.handleError)
      );
  }

  // ✅ Méthode de connexion corrigée
  login(user: User): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    // 🔍 Log pour debug (sans le mot de passe)
    console.log('🔐 Tentative de connexion:', {
      email: user.email,
      username: user.username,
      hasPassword: !!user.password
    });

    // Nettoyer les données avant envoi
    const cleanCredentials = {
      email: user.email?.trim().toLowerCase(),
      username: user.username?.trim(),
      password: user.password
    };

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, cleanCredentials, { headers })
      .pipe(
        tap(response => {
          console.log('✅ Réponse du serveur:', {
            hasToken: !!response.token,
            hasUser: !!response.user,
            userEmail: response.user?.email
          });
        }),
        catchError(this.handleError)
      );
  }

  // ✅ Méthode de réinitialisation de mot de passe corrigée
  resetPassword(email: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(`${this.apiUrl}/reset-password`, { email }, { headers })
      .pipe(catchError(this.handleError));
  }

  // ✅ Confirmation de réinitialisation corrigée
  resetPasswordConfirm(code: string, newPassword: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post(`${this.apiUrl}/confirm-reset-password`, {
      resetCode: code,
      newPassword: newPassword
    }, { headers }).pipe(catchError(this.handleError));
  }

  // ✅ Récupération des utilisateurs corrigée
  getAllUsers(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  // ✅ Mise à jour du statut utilisateur corrigée
  updateUserStatus(id: number, active: boolean): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${this.apiUrl}/${id}/status?active=${active}`, null)
      .pipe(catchError(this.handleError));
  }

  // ✅ Gestion centralisée des erreurs
  private handleError = (error: HttpErrorResponse) => {
    console.error('❌ Erreur HTTP:', {
      status: error.status,
      statusText: error.statusText,
      message: error.error?.message || error.message,
      url: error.url
    });

    let errorMessage = 'Erreur de connexion';
    
    switch (error.status) {
      case 401:
        errorMessage = error.error?.message || 'Identifiants invalides';
        break;
      case 404:
        errorMessage = 'Service introuvable';
        break;
      case 500:
        errorMessage = 'Erreur serveur interne';
        break;
      case 0:
        errorMessage = 'Impossible de joindre le serveur';
        break;
      default:
        errorMessage = error.error?.message || 'Erreur inconnue';
    }

    return throwError(() => ({ ...error, message: errorMessage }));
  }

  // ✅ Méthodes utilitaires
  testConnection(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`).pipe(
      tap(response => console.log('✅ Serveur accessible:', response)),
      catchError(error => {
        console.error('❌ Serveur inaccessible:', error);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }
}