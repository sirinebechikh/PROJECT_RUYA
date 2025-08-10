 // dashboard.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, timer, EMPTY, of } from 'rxjs';
import { catchError, retry, tap, switchMap, startWith, delay, filter, take } from 'rxjs/operators';

export interface DataRow {
  label: string;
  value: number | string;
  amount?: string;
  status?: 'success' | 'warning' | 'danger';
}

export interface CardData {
  title: string;
  icon: string;
  type: 'primary' | 'success' | 'warning' | 'default';
  data: DataRow[];
}

export interface StatCard {
  number: string;
  label: string;
  amount?: string;
  status?: string;
}

export interface DashboardResponse {
  cardData: CardData[];
  globalStats: StatCard[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  
  // Configuration de l'API
  private readonly API_BASE_URL = 'http://localhost:8081/api/dashboard';
  private readonly MAX_CONSECUTIVE_ERRORS = 3;
  private readonly BASE_RETRY_DELAY = 5000; // 5 seconds
  
  // Sujets pour la gestion d'état réactive
  private dashboardDataSubject = new BehaviorSubject<DashboardResponse | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  private autoRefreshEnabledSubject = new BehaviorSubject<boolean>(true);
  
  // Compteur d'erreurs consécutives
  private consecutiveErrors = 0;
  
  // Observables publics
  public dashboardData$ = this.dashboardDataSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();
  public autoRefreshEnabled$ = this.autoRefreshEnabledSubject.asObservable();
  
  constructor() {
    // Démarrer le rafraîchissement automatique
    this.startAutoRefresh();
  }

  /**
   * Récupère les données du dashboard depuis l'API
   */
  getDashboardData(): Observable<DashboardResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    
    return this.http.get<DashboardResponse>(`${this.API_BASE_URL}/data`).pipe(
      retry({
        count: 2,
        delay: (error, retryCount) => {
          console.warn(`Tentative ${retryCount} après erreur:`, error);
          return timer(1000 * retryCount); // Délai progressif: 1s, 2s
        }
      }),
      tap(data => {
        this.dashboardDataSubject.next(data);
        this.loadingSubject.next(false);
        this.consecutiveErrors = 0; // Reset error counter on success
        this.autoRefreshEnabledSubject.next(true); // Re-enable auto refresh on success
        console.log('Données du dashboard chargées:', data);
      }),
      catchError(this.handleError.bind(this))
    );
  }

  /**
   * Rafraîchit manuellement les données
   */
  refreshData(): Observable<DashboardResponse> {
    return this.http.post<DashboardResponse>(`${this.API_BASE_URL}/refresh`, {}).pipe(
      tap(data => {
        this.dashboardDataSubject.next(data);
        this.loadingSubject.next(false);
        this.consecutiveErrors = 0;
        this.autoRefreshEnabledSubject.next(true);
        console.log('Données rafraîchies:', data);
      }),
      catchError(this.handleError.bind(this))
    );
  }

  /**
   * Démarre le rafraîchissement automatique avec gestion d'erreurs intelligente
   */
  private startAutoRefresh(): void {
    timer(0, 30000).pipe(
      // Only proceed if auto-refresh is enabled
      filter(() => this.autoRefreshEnabledSubject.value),
      switchMap(() => {
        // Check if we should pause due to consecutive errors
        if (this.consecutiveErrors >= this.MAX_CONSECUTIVE_ERRORS) {
          const pauseDuration = this.BASE_RETRY_DELAY * Math.pow(2, this.consecutiveErrors - this.MAX_CONSECUTIVE_ERRORS);
          console.warn(`Pause du rafraîchissement automatique pendant ${pauseDuration/1000}s après ${this.consecutiveErrors} erreurs consécutives`);
          
          // Pause and then try a health check
          return timer(pauseDuration).pipe(
            switchMap(() => this.healthCheck()),
            switchMap(() => {
              console.log('Serveur de nouveau disponible, reprise du rafraîchissement');
              this.consecutiveErrors = 0;
              return this.getDashboardData();
            }),
            catchError(error => {
              console.error('Serveur toujours indisponible:', error);
              this.consecutiveErrors++;
              return EMPTY; // Don't emit anything, continue the timer
            })
          );
        }
        
        // Normal refresh attempt
        return this.getDashboardData().pipe(
          catchError(error => {
            this.consecutiveErrors++;
            console.error(`Erreur lors du rafraîchissement automatique (${this.consecutiveErrors}/${this.MAX_CONSECUTIVE_ERRORS}):`, error);
            
            // If we've hit max errors, disable auto-refresh temporarily
            if (this.consecutiveErrors >= this.MAX_CONSECUTIVE_ERRORS) {
              this.autoRefreshEnabledSubject.next(false);
              console.warn('Rafraîchissement automatique mis en pause après trop d\'erreurs consécutives');
            }
            
            return EMPTY; // Don't emit anything, continue the timer
          })
        );
      })
    ).subscribe();
  }

  /**
   * Force la reprise du rafraîchissement automatique
   */
  resumeAutoRefresh(): void {
    this.consecutiveErrors = 0;
    this.autoRefreshEnabledSubject.next(true);
    this.clearError();
    console.log('Rafraîchissement automatique repris manuellement');
  }

  /**
   * Arrête temporairement le rafraîchissement automatique
   */
  pauseAutoRefresh(): void {
    this.autoRefreshEnabledSubject.next(false);
    console.log('Rafraîchissement automatique mis en pause manuellement');
  }

  /**
   * Vérifie la santé du service
   */
  healthCheck(): Observable<string> {
    return this.http.get(`${this.API_BASE_URL}/health`, { 
      observe: 'body',
      responseType: 'text'
    }).pipe(
      catchError(error => {
        console.warn('Health check échoué:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Test de connectivité simple
   */
  testConnection(): Observable<boolean> {
    return this.healthCheck().pipe(
      take(1),
      tap(() => console.log('Test de connexion réussi')),
      switchMap(() => of(true)),
      catchError(() => {
        console.warn('Test de connexion échoué');
        return of(false);
      })
    );
  }

  /**
   * Gère les erreurs HTTP avec plus de détails
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur est survenue';
    
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de contacter le serveur. Vérifiez votre connexion et que le serveur backend est démarré sur le port 8081.';
          break;
        case 404:
          errorMessage = 'Service non trouvé. Vérifiez l\'URL de l\'API (/api/dashboard/data).';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur. Vérifiez les logs du serveur backend.';
          break;
        case 503:
          errorMessage = 'Service temporairement indisponible. Réessayez dans quelques instants.';
          break;
        default:
          errorMessage = `Erreur serveur: ${error.status} - ${error.message}`;
      }
    }
    
    this.errorSubject.next(errorMessage);
    this.loadingSubject.next(false);
    console.error('Erreur Dashboard Service:', errorMessage, error);
    
    return throwError(() => errorMessage);
  }

  /**
   * Méthodes utilitaires pour les composants
   */
  getCurrentData(): DashboardResponse | null {
    return this.dashboardDataSubject.value;
  }

  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  getLastError(): string | null {
    return this.errorSubject.value;
  }

  getConsecutiveErrorCount(): number {
    return this.consecutiveErrors;
  }

  isAutoRefreshEnabled(): boolean {
    return this.autoRefreshEnabledSubject.value;
  }

  clearError(): void {
    this.errorSubject.next(null);
  }
}