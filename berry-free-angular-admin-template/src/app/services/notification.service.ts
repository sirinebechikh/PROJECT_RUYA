import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Notification {
  id: number;
  type: 'AJOUT' | 'ENVOI' | 'RECEPTION';
  titre: string;
  message: string;
  fichier?: any;
  userAction?: {
    id: number;
    username: string;
    email: string;
    role: string;
  };
  timestamp: string;
  lu: boolean;
  icon: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl = 'http://localhost:8081/api/notifications';
  
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();
  
  private unreadCountSubject = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Charger toutes les notifications
  loadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.baseUrl).pipe(
      tap(notifications => {
        this.notificationsSubject.next(notifications);
        this.updateUnreadCount();
      })
    );
  }

  // Charger les notifications non lues
  loadNotificationsNonLues(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/non-lues`).pipe(
      tap(notifications => {
        console.log('🔍 DEBUG - Notifications non lues chargées:', notifications.length);
        this.notificationsSubject.next(notifications);
        this.updateUnreadCount();
      })
    );
  }

  // Marquer une notification comme lue
  marquerCommeLue(notificationId: number): Observable<Notification> {
    console.log('🔍 DEBUG - Marquer notification comme lue:', notificationId);
    return this.http.put<Notification>(`${this.baseUrl}/${notificationId}/marquer-lue`, {}).pipe(
      tap((updatedNotification) => {
        console.log('🔍 DEBUG - Notification marquée comme lue:', updatedNotification);
        // Mettre à jour le cache local
        this.updateLocalCache(updatedNotification);
        // Recharger immédiatement toutes les notifications
        this.loadNotifications().subscribe();
      })
    );
  }
  
  // Mettre à jour le cache local
  private updateLocalCache(updatedNotification: Notification): void {
    const currentNotifications = this.notificationsSubject.value;
    const updatedNotifications = currentNotifications.map(notification => 
      notification.id === updatedNotification.id ? updatedNotification : notification
    );
    this.notificationsSubject.next(updatedNotifications);
    
    // Sauvegarder dans localStorage
    localStorage.setItem('notifications_cache', JSON.stringify(updatedNotifications));
    localStorage.setItem('notifications_timestamp', new Date().toISOString());
    console.log('🔍 DEBUG - Cache local mis à jour avec notification:', updatedNotification.id);
  }

  // Marquer toutes les notifications comme lues
  marquerToutesCommeLues(): Observable<void> {
    console.log('🔍 DEBUG - Marquer toutes les notifications comme lues');
    return this.http.put<void>(`${this.baseUrl}/marquer-toutes-lues`, {}).pipe(
      tap(() => {
        console.log('🔍 DEBUG - Toutes les notifications marquées comme lues');
        // Mettre à jour le cache local
        this.updateAllNotificationsAsRead();
        // Recharger immédiatement toutes les notifications
        this.loadNotifications().subscribe();
      })
    );
  }
  
  // Mettre à jour toutes les notifications comme lues dans le cache
  private updateAllNotificationsAsRead(): void {
    const currentNotifications = this.notificationsSubject.value;
    const updatedNotifications = currentNotifications.map(notification => ({
      ...notification,
      lu: true
    }));
    this.notificationsSubject.next(updatedNotifications);
    
    // Sauvegarder dans localStorage
    localStorage.setItem('notifications_cache', JSON.stringify(updatedNotifications));
    localStorage.setItem('notifications_timestamp', new Date().toISOString());
  }

  // Obtenir le nombre de notifications non lues
  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count-non-lues`);
  }

  // Mettre à jour le compteur de notifications non lues
  private updateUnreadCount(): void {
    this.getUnreadCount().subscribe(count => {
      this.unreadCountSubject.next(count);
    });
  }

  // Obtenir le temps écoulé
  getTimeAgo(timestamp: string): string {
    const now = new Date();
    const date = new Date(timestamp);
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `${minutes} min ago`;
    if (hours < 24) return `${hours} h ago`;
    return `${days} j ago`;
  }
} 