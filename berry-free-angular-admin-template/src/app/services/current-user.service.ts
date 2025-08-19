import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  role: string;
  isAdmin?: boolean;
  name?: string;
  roles?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class CurrentUserService {
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    // Charger l'utilisateur depuis localStorage au démarrage
    this.loadUserFromStorage();
  }

  /**
   * Charge l'utilisateur depuis localStorage
   */
  private loadUserFromStorage(): void {
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
        console.log('👤 Utilisateur chargé depuis localStorage:', user);
      }
    } catch (error) {
      console.error('❌ Erreur lors du chargement de l\'utilisateur:', error);
      this.clearUser();
    }
  }

  /**
   * Définit l'utilisateur courant
   */
  setCurrentUser(user: CurrentUser): void {
    try {
      localStorage.setItem('user', JSON.stringify(user));
      this.currentUserSubject.next(user);
      console.log('👤 Utilisateur défini:', user);
    } catch (error) {
      console.error('❌ Erreur lors de la sauvegarde de l\'utilisateur:', error);
    }
  }

  /**
   * Obtient l'utilisateur courant (synchrone)
   */
  getCurrentUser(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Obtient l'ID de l'utilisateur courant
   */
  getCurrentUserId(): number | null {
    const user = this.getCurrentUser();
    return user?.id || null;
  }

  /**
   * Obtient le nom d'utilisateur courant
   */
  getCurrentUsername(): string {
    const user = this.getCurrentUser();
    return user?.username || user?.name || 'Utilisateur inconnu';
  }

  /**
   * Vérifie si un utilisateur est connecté
   */
  isLoggedIn(): boolean {
    const user = this.getCurrentUser();
    return user !== null && user.id !== undefined && user.id > 0;
  }

  /**
   * Vérifie si l'utilisateur courant est admin
   */
  isAdmin(): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    
    return user.role === 'ADMIN' || 
           user.roles?.includes('ADMIN') || 
           user.username === 'admin' ||
           user.isAdmin === true;
  }

  /**
   * Obtient les informations complètes de l'utilisateur
   */
  getUserInfo(): { id: number; username: string; email: string; role: string } | null {
    const user = this.getCurrentUser();
    if (!user) return null;

    return {
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role
    };
  }

  /**
   * Efface l'utilisateur courant (déconnexion)
   */
  clearUser(): void {
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    console.log('👤 Utilisateur déconnecté');
  }

  /**
   * Recharge l'utilisateur depuis localStorage
   */
  refreshUser(): void {
    this.loadUserFromStorage();
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   */
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;

    return user.role === role || user.roles?.includes(role) || false;
  }

  /**
   * Obtient l'objet utilisateur formaté pour l'API
   */
  getUserForApi(): { id: number } | null {
    const userId = this.getCurrentUserId();
    return userId ? { id: userId } : null;
  }

  /**
   * Valide que l'utilisateur est connecté et retourne son ID
   * Lance une erreur si l'utilisateur n'est pas connecté
   */
  requireUserId(): number {
    const userId = this.getCurrentUserId();
    if (!userId) {
      throw new Error('Utilisateur non connecté. Veuillez vous reconnecter.');
    }
    return userId;
  }

  /**
   * Obtient les informations de l'utilisateur pour les logs
   */
  getUserLogInfo(): string {
    const user = this.getCurrentUser();
    if (!user) return 'Utilisateur non connecté';
    
    return `${user.username} (ID: ${user.id}, Role: ${user.role})`;
  }
}