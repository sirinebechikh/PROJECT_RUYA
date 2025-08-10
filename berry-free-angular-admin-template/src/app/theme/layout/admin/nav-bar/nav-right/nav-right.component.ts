// Angular import
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

// third party import
import { SharedModule } from 'src/app/theme/shared/shared.module';
import { AjouterFichierService } from 'src/app/demo/ajouter-fichier/ajouter-fichier.service';
import { NotificationService, Notification } from 'src/app/services/notification.service';

interface Fichier {
  id: number;
  nomFichier: string;
  codEn: string;
  codeValeur: string;
  createdAt: string;
  natureFichier: string;
  sens: string;
  typeFichier: string;
  updatedAt: string;
  montant: string;
  nomber: string;
  user?: {
    id: number;
    username: string;
    email: string;
    role: string;
  };
}



@Component({
  selector: 'app-nav-right',
  imports: [RouterModule, SharedModule, FormsModule, CommonModule],
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent implements OnInit, OnDestroy {
  userJson: any = null;
  
  // Propriétés pour la recherche globale
  globalSearchTerm = '';
  filteredGlobalResults: Fichier[] = [];
  showSearchResults = false;
  allFichiers: Fichier[] = [];
  subscription: Subscription;
  
  // Propriétés pour la popup de détails
  showFileDetailsPopup = false;
  selectedFile: Fichier | null = null;

  // Propriétés pour les notifications
  notifications: Notification[] = [];
  unreadCount = 0;
  showNotifications = false;

  constructor(
    private router: Router,
    private ajouterFichierService: AjouterFichierService,
    public notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    this.userJson = userStr ? JSON.parse(userStr) : null;
    
    console.log('🔍 DEBUG - Utilisateur chargé:', this.userJson);
    console.log('🔍 DEBUG - Est ADMIN:', this.isAdminUser());
    
    // Charger les notifications depuis le backend
    this.chargerNotificationsBackend();
    
    // Charger aussi depuis le cache pour une meilleure persistance
    this.chargerNotificationsDepuisCache();
    
    // Charger tous les fichiers pour la recherche globale
    this.subscription = this.ajouterFichierService.fichiers$.subscribe(data => {
      const anciensFichiers = this.allFichiers;
      this.allFichiers = data || [];
      
      console.log('Fichiers chargés dans la recherche globale:', this.allFichiers.length);
      console.log('Types de fichiers disponibles:', [...new Set(this.allFichiers.map(f => f.typeFichier))]);
    });
    
    // Charger les fichiers depuis le service
    this.loadAllFichiers();
    
    // Recharger les notifications toutes les 30 secondes pour rester synchronisé
    setInterval(() => {
      if (this.isAdminUser()) {
        this.chargerNotificationsBackend();
      }
    }, 30000);
  }

  // Méthode pour charger les notifications depuis le backend
  private chargerNotificationsBackend() {
    console.log('🔍 DEBUG - Chargement des notifications depuis le backend...');
    
    // Charger TOUTES les notifications (lues et non lues)
    this.notificationService.loadNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        // Compter seulement les notifications non lues pour le badge
        this.unreadCount = notifications.filter(n => !n.lu).length;
        console.log('🔍 DEBUG - Toutes les notifications chargées depuis le backend:', notifications.length);
        console.log('🔍 DEBUG - Notifications non lues:', this.unreadCount);
        
        // Sauvegarder les notifications dans localStorage pour la persistance
        this.sauvegarderNotificationsLocales();
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des notifications:', error);
        // En cas d'erreur, essayer de charger depuis le cache
        this.chargerNotificationsDepuisCache();
      }
    });
  }
  
  // Méthode pour charger les notifications depuis le cache
  private chargerNotificationsDepuisCache() {
    const cachedNotifications = localStorage.getItem('notifications_cache');
    const timestamp = localStorage.getItem('notifications_timestamp');
    
    if (cachedNotifications && timestamp) {
      const cacheAge = new Date().getTime() - new Date(timestamp).getTime();
      const maxAge = 5 * 60 * 1000; // 5 minutes
      
      if (cacheAge < maxAge) {
        try {
          this.notifications = JSON.parse(cachedNotifications);
          this.unreadCount = this.notifications.length;
          console.log('🔍 DEBUG - Notifications chargées depuis le cache:', this.notifications.length);
        } catch (error) {
          console.error('❌ Erreur lors du chargement du cache:', error);
        }
      }
    }
  }

  // Méthode pour charger tous les fichiers
  loadAllFichiers() {
    this.ajouterFichierService.getAllFichiers().subscribe({
      next: (data) => {
        console.log('Données reçues du service:', data?.length || 0);
        if (data && data.length > 0) {
          console.log('Exemples de fichiers:', data.slice(0, 3));
        }
        this.ajouterFichierService.setFichiers(data);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des fichiers', err);
        // En cas d'erreur, essayer de recharger après un délai
        setTimeout(() => this.loadAllFichiers(), 2000);
      }
    });
  }

  ngOnDestroy() {
    if (this.subscription) this.subscription.unsubscribe();
  }

  // Méthode pour la recherche globale
  onGlobalSearchChange() {
    if (this.globalSearchTerm.trim()) {
      const searchLower = this.globalSearchTerm.toLowerCase();
      this.filteredGlobalResults = this.allFichiers.filter(fichier => {
        // Vérifier que le fichier existe et a les propriétés nécessaires
        if (!fichier) return false;
        
        return (
          (fichier.nomFichier && fichier.nomFichier.toLowerCase().includes(searchLower)) ||
          (fichier.codEn && fichier.codEn.toLowerCase().includes(searchLower)) ||
          (fichier.codeValeur && fichier.codeValeur.toLowerCase().includes(searchLower)) ||
          (fichier.natureFichier && fichier.natureFichier.toLowerCase().includes(searchLower)) ||
          (fichier.typeFichier && fichier.typeFichier.toLowerCase().includes(searchLower)) ||
          (fichier.sens && fichier.sens.toLowerCase().includes(searchLower)) ||
          (fichier.montant && fichier.montant.toString().toLowerCase().includes(searchLower)) ||
          (fichier.nomber && fichier.nomber.toString().toLowerCase().includes(searchLower))
        );
      });
      
      // Debug: afficher les résultats trouvés
      console.log('Recherche:', this.globalSearchTerm);
      console.log('Tous les fichiers:', this.allFichiers.length);
      console.log('Résultats trouvés:', this.filteredGlobalResults.length);
      console.log('Types de fichiers trouvés:', [...new Set(this.filteredGlobalResults.map(f => f.typeFichier))]);
    } else {
      this.filteredGlobalResults = [];
    }
  }

  // Méthode pour effacer la recherche
  clearGlobalSearch() {
    this.globalSearchTerm = '';
    this.filteredGlobalResults = [];
    this.showSearchResults = false;
  }

  // Méthode pour gérer le blur de la recherche
  onSearchBlur() {
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  // Méthode pour afficher les détails du fichier
  showFileDetails(fichier: Fichier) {
    this.selectedFile = fichier;
    this.showFileDetailsPopup = true;
    this.showSearchResults = false;
  }

  // Méthode pour fermer la popup
  closeFileDetailsPopup() {
    this.showFileDetailsPopup = false;
    this.selectedFile = null;
  }

  // Méthode pour naviguer vers le fichier sélectionné
  navigateToFile(fichier: Fichier) {
    let route = '';
    
    switch (fichier.typeFichier) {
      case 'cheque':
        route = `/cheque/${fichier.codeValeur}`;
        break;
      case 'effet':
        route = `/effet/${fichier.codeValeur}`;
        break;
      case 'virement':
        route = `/virement/${fichier.codeValeur}`;
        break;
      case 'prelevement':
        route = `/prlv/${fichier.codeValeur}`;
        break;
      default:
        route = '/default';
    }
    
    // Sauvegarder l'ID du fichier à mettre en évidence
    localStorage.setItem('highlightedFileId', fichier.id.toString());
    
    // Fermer la popup
    this.closeFileDetailsPopup();
    
    // Naviguer vers la page
    this.router.navigate([route]);
  }

  // Méthode pour obtenir l'icône selon le type de fichier
  getFileTypeIcon(typeFichier: string): string {
    switch (typeFichier) {
      case 'cheque':
        return 'ti ti-receipt';
      case 'effet':
        return 'ti ti-file-text';
      case 'virement':
        return 'ti ti-exchange';
      case 'prelevement':
        return 'ti ti-arrow-down';
      default:
        return 'ti ti-file';
    }
  }

  onLogout() {
    // Ici, ajouter la logique de déconnexion réelle si besoin
    this.router.navigate(['/guest/login']);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }



  // Méthode pour marquer une notification comme lue
  marquerCommeLue(notification: Notification) {
    console.log('🔍 DEBUG - Marquer comme lue:', notification.id);
    
    // Mettre à jour immédiatement l'interface
    notification.lu = true;
    this.unreadCount = this.notifications.filter(n => !n.lu).length;
    
    // Sauvegarder l'état local immédiatement
    this.sauvegarderNotificationsLocales();
    
    this.notificationService.marquerCommeLue(notification.id).subscribe({
      next: (updatedNotification) => {
        console.log('🔍 DEBUG - Notification marquée comme lue:', updatedNotification);
        // Mettre à jour la notification dans la liste locale
        const index = this.notifications.findIndex(n => n.id === notification.id);
        if (index !== -1) {
          this.notifications[index] = updatedNotification;
        }
        // Recharger depuis le backend pour synchronisation
        this.chargerNotificationsBackend();
      },
      error: (error) => {
        console.error('❌ Erreur lors du marquage de la notification:', error);
        // En cas d'erreur, remettre l'état précédent
        notification.lu = false;
        this.unreadCount = this.notifications.filter(n => !n.lu).length;
        this.sauvegarderNotificationsLocales();
      }
    });
  }
  
  // Méthode pour naviguer vers le fichier depuis une notification
  naviguerVersFichierDepuisNotification(notification: Notification) {
    console.log('🔍 DEBUG - Méthode naviguerVersFichierDepuisNotification appelée');
    console.log('🔍 DEBUG - Notification:', notification);
    
    // Essayer de récupérer le fichier depuis la notification ou depuis le message
    let fichier = notification.fichier;
    
    if (!fichier) {
      console.log('🔍 DEBUG - Aucun fichier direct dans la notification, tentative de récupération...');
      // Essayer de récupérer le fichier depuis le message de la notification
      const message = notification.message;
      console.log('🔍 DEBUG - Message de la notification:', message);
      
      // Chercher dans tous les fichiers disponibles
      const fichierTrouve = this.allFichiers.find(f => 
        message.includes(f.nomFichier) || 
        message.includes(f.codeValeur) ||
        message.includes(f.codEn)
      );
      
      if (fichierTrouve) {
        fichier = fichierTrouve;
        console.log('🔍 DEBUG - Fichier trouvé via recherche dans le message:', fichier);
      }
    }
    
    if (fichier) {
      console.log('🔍 DEBUG - Fichier trouvé dans la notification:', fichier);
      console.log('🔍 DEBUG - Type de fichier:', fichier.typeFichier);
      console.log('🔍 DEBUG - Code valeur:', fichier.codeValeur);
      
      // Sauvegarder l'ID du fichier à mettre en évidence
      localStorage.setItem('highlightedFileId', fichier.id.toString());
      console.log('🔍 DEBUG - ID sauvegardé:', fichier.id);
      
      // Naviguer vers la page appropriée
      let route = '';
      switch (fichier.typeFichier) {
        case 'cheque':
          route = `/cheque/${fichier.codeValeur}`;
          break;
        case 'effet':
          route = `/effet/${fichier.codeValeur}`;
          break;
        case 'virement':
          route = `/virement/${fichier.codeValeur}`;
          break;
        case 'prelevement':
          route = `/prlv/${fichier.codeValeur}`;
          break;
        default:
          route = '/default';
      }
      
      console.log('🔍 DEBUG - Route calculée:', route);
      
      // Fermer les notifications
      this.showNotifications = false;
      
      // Marquer la notification comme lue
      this.marquerCommeLue(notification);
      
      // Naviguer vers la page
      console.log('🔍 DEBUG - Navigation vers:', route);
      this.router.navigate([route]).then(() => {
        console.log('🔍 DEBUG - Navigation réussie vers:', route);
      }).catch(error => {
        console.error('❌ Erreur lors de la navigation:', error);
      });
    } else {
      console.error('❌ Aucun fichier trouvé dans la notification ou dans les fichiers disponibles');
      alert('Impossible de trouver le fichier associé à cette notification.');
    }
  }
  
  // Méthode pour sauvegarder les notifications localement
  private sauvegarderNotificationsLocales(): void {
    localStorage.setItem('notifications_cache', JSON.stringify(this.notifications));
    localStorage.setItem('notifications_timestamp', new Date().toISOString());
    console.log('🔍 DEBUG - Notifications sauvegardées localement:', this.notifications.length);
  }

  // Méthode pour marquer toutes les notifications comme lues
  marquerToutesCommeLues() {
    console.log('🔍 DEBUG - Marquer toutes comme lues');
    
    // Mettre à jour immédiatement l'interface
    this.notifications.forEach(n => n.lu = true);
    this.unreadCount = 0;
    
    // Sauvegarder l'état local immédiatement
    this.sauvegarderNotificationsLocales();
    
    this.notificationService.marquerToutesCommeLues().subscribe({
      next: () => {
        console.log('🔍 DEBUG - Toutes les notifications marquées comme lues');
        // Recharger depuis le backend pour synchronisation
        this.chargerNotificationsBackend();
      },
      error: (error) => {
        console.error('❌ Erreur lors du marquage de toutes les notifications:', error);
        // En cas d'erreur, remettre l'état précédent
        this.notifications.forEach(n => n.lu = false);
        this.unreadCount = this.notifications.length;
        this.sauvegarderNotificationsLocales();
      }
    });
  }



  // Méthode pour basculer l'affichage des notifications
  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }

  // Méthode pour obtenir le temps écoulé
  getTimeAgo(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `${minutes} min ago`;
    if (hours < 24) return `${hours} h ago`;
    return `${days} j ago`;
  }

  

  // Méthode pour obtenir le titre de la notification
  getNotificationTitle(type: string, fichier: any): string {
    switch (type) {
      case 'ajout':
        return `Nouveau fichier ${fichier.typeFichier}`;
      case 'envoi':
        return `Fichier ${fichier.typeFichier} envoyé`;
      case 'reception':
        return `Fichier ${fichier.typeFichier} reçu`;
      default:
        return 'Notification';
    }
  }

  // Méthode pour obtenir le message de la notification
  getNotificationMessage(type: string, fichier: any, username?: string): string {
    const userInfo = username ? ` par ${username}` : '';
    
    switch (type) {
      case 'ajout':
        return `Le fichier "${fichier.nomFichier}" a été ajouté${userInfo}.`;
      case 'envoi':
        return `Le fichier "${fichier.nomFichier}" a été envoyé${userInfo}.`;
      case 'reception':
        return `Le fichier "${fichier.nomFichier}" a été reçu${userInfo}.`;
      default:
        return 'Nouvelle notification';
    }
  }

  // Méthode pour animer la notification
  animerNotification() {
    // Ajouter une classe CSS pour l'animation
    const badge = document.querySelector('.notification-badge');
    if (badge) {
      badge.classList.add('notification-pulse');
      setTimeout(() => {
        badge.classList.remove('notification-pulse');
      }, 1000);
    }
  }

  // Méthode pour vérifier si l'utilisateur actuel est ADMIN
  isAdminUser(): boolean {
    if (!this.userJson) return false;
    
    // Vérifier si l'utilisateur a le rôle ADMIN
    // Vous pouvez adapter cette logique selon votre structure de données utilisateur
    return this.userJson.role === 'ADMIN' || 
           this.userJson.roles?.includes('ADMIN') || 
           this.userJson.username === 'admin' ||
           this.userJson.isAdmin === true;
  }

  // Méthode pour obtenir le nom d'utilisateur actuel
  getCurrentUsername(): string {
    if (!this.userJson) return 'Utilisateur inconnu';
    return this.userJson.username || this.userJson.name || 'Utilisateur inconnu';
  }

}
