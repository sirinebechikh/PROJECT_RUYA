 // default.component.ts - Version finale avec intégration backend
import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { DashboardService, DashboardResponse, CardData, StatCard, DataRow } from './dashboard.service';

@Component({
  selector: 'app-default',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './default.component.html',
  styleUrls: ['./default.component.scss'],
  providers: [DashboardService]
})
export class DefaultComponent implements OnInit, OnDestroy {
  private dashboardService = inject(DashboardService);
  private subscriptions: Subscription[] = [];
  
  // Signals for reactive state management
  currentDate = signal(new Date().toISOString().split('T')[0]);
  isLoading = signal(false);
  sessionActive = signal(true);
  errorMessage = signal<string | null>(null);
  connectionStatus = signal<'connected' | 'connecting' | 'disconnected'>('connecting');
  lastUpdateTime = signal<string>('');
  autoRefreshEnabled = signal(true);
  
  // Dashboard data - sera alimenté par l'API backend
  cardData = signal<CardData[]>([]);
  globalStats = signal<StatCard[]>([]);

  // Données mock par défaut (utilisées si le backend n'est pas disponible)
  private mockCardData: CardData[] = [
    {
      title: 'ENCAISSE_VALEUR',
      icon: 'fas fa-globe',
      type: 'success',
      data: [
        { label: 'Remises créées', value: 34, amount: '45 670 €' },
        { label: 'Chèques web', value: 142, amount: '19 580 €' },
      ]
    },  
    {
      title: 'FICHIERS_GENERER par Encaisse',
      icon: 'fas fa-cogs',
      type: 'default',
      data: [
        { label: 'Nombre de remises', value: 134, amount: '189 670 €' },
        { label: 'Total des remises', value: 290, amount: '412 400 €' }
      ]
    },
     
    {
      title: 'Fichiers Carthago',
      icon: 'fas fa-server',
      type: 'default',
      data: [
        { label: 'Consommés par Carthago', value: 1198, amount: '156 780 €' },
        { label: 'Générés vers CTR', value: 1156, amount: '148 920 €' },
        { label: 'Reçus par CTR', value: 1089, amount: '142 350 €' }
      ]
    },

    {
      title: 'Carthago avant CTR',
      icon: 'fas fa-exchange-alt',
      type: 'warning',
      data: [
        { label: 'Nombre de remises', value: 38, amount: '52 840 €' },
        { label: 'Images STATUT 3', value: 156, status: 'warning' },
        { label: 'Chèques fichier', value: 289, amount: '41 260 €' }
      ]
    },
    {
      title: 'Remises CTR',
      icon: 'fas fa-copy',
      type: 'default',
      data: [
        { label: 'Remises en double', value: 15, amount: '23 450 €' },
        { label: 'Chèques électroniques', value: 145, amount: '18 940 €' },
        { label: 'Chèques fichier ENV', value: 223, amount: '28 560 €' }
      ]
    },
    {
      title: 'Actions et Contrôles',
      icon: 'fas fa-tools',
      type: 'default',
      data: [
        { label: 'Remises non parvenues', value: 'En attente', status: 'warning' },
        { label: 'CARTHAGO après CTR', value: 'OK', status: 'success' },
       ]
    }
  ];

  private mockGlobalStats: StatCard[] = [
    { number: '2 847', label: 'Total Remises', amount: '1 234 567 DT' },
    { number: '15 692', label: 'Total Chèques', amount: '567 890 €' },
    { number: '98.5%', label: 'Taux de Réussite', status: 'Excellent' },
    { number: '14:30', label: 'Dernière MAJ' }
  ];

  // Computed values
  totalAmount = computed(() => {
    let total = 0;
    this.cardData().forEach(card => {
      card.data.forEach(row => {
        if (row.amount) {
          const amount = parseFloat(row.amount.replace(/[^\d.-]/g, ''));
          total += amount || 0;
        }
      });
    });
    return total.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' });
  });

  // Computed connection status display
  connectionStatusDisplay = computed(() => {
    switch (this.connectionStatus()) {
      case 'connected': return 'Backend connecté';
      case 'connecting': return 'Connexion au backend...';
      case 'disconnected': return 'Mode hors-ligne';
      default: return 'État inconnu';
    }
  });

  ngOnInit() {
    console.log('🚀 Initialisation du composant RUYA Dashboard');
    this.initializeDashboard();
    this.subscribeToDataUpdates();
  }

  ngOnDestroy() {
    console.log('🛑 Fermeture du composant RUYA Dashboard');
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  /**
   * Initialise le dashboard avec données mock puis essaie de se connecter au backend
   */
  private initializeDashboard(): void {
    // Afficher immédiatement les données mock pour une meilleure UX
    this.cardData.set(this.mockCardData);
    this.globalStats.set(this.mockGlobalStats);
    this.lastUpdateTime.set(new Date().toLocaleString('fr-FR'));
    this.connectionStatus.set('connecting');
    
    console.log('📊 Données mock chargées, tentative de connexion au backend...');
    
    // Tester la connexion backend
    this.testBackendConnection();
  }

  /**
   * Test de connexion au backend Spring Boot
   */
  private testBackendConnection(): void {
    this.dashboardService.testConnection().subscribe({
      next: (isConnected) => {
        if (isConnected) {
          this.connectionStatus.set('connected');
          this.showNotification('✅ Backend Spring Boot connecté !', 'success');
          console.log('🔗 Connexion backend établie, chargement des données réelles...');
          this.loadRealDataFromBackend();
        } else {
          this.handleOfflineMode();
        }
      },
      error: (error) => {
        console.warn('⚠️ Impossible de se connecter au backend:', error);
        this.handleOfflineMode();
      }
    });
  }

  /**
   * Charge les données réelles depuis le backend
   */
  private loadRealDataFromBackend(): void {
    this.dashboardService.getDashboardData().subscribe({
      next: (data) => {
        console.log('📈 Données réelles chargées depuis le backend:', data);
        this.showNotification('📊 Données en temps réel chargées !', 'success');
        // Les données sont automatiquement mises à jour via les observables
      },
      error: (error) => {
        console.error('❌ Échec du chargement des données réelles:', error);
        this.showNotification('⚠️ Échec du chargement, conservation des données de démonstration', 'warning');
        this.handleOfflineMode();
      }
    });
  }

  /**
   * Gère le mode hors-ligne
   */
  private handleOfflineMode(): void {
    this.connectionStatus.set('disconnected');
    this.errorMessage.set('Backend Spring Boot non disponible. Utilisation des données de démonstration.');
    this.showNotification('📴 Mode hors-ligne : données de démonstration', 'warning');
  }

  /**
   * S'abonne aux mises à jour automatiques du service
   */
  private subscribeToDataUpdates(): void {
    // Données du dashboard
    const dataSubscription = this.dashboardService.dashboardData$.subscribe(data => {
      if (data) {
        console.log('🔄 Mise à jour des données depuis le backend');
        this.cardData.set(data.cardData);
        this.globalStats.set(data.globalStats);
        this.lastUpdateTime.set(new Date().toLocaleString('fr-FR'));
        this.connectionStatus.set('connected');
        this.clearError();
      }
    });

    // État de chargement
    const loadingSubscription = this.dashboardService.loading$.subscribe(loading => {
      this.isLoading.set(loading);
      if (loading && this.connectionStatus() !== 'disconnected') {
        this.connectionStatus.set('connecting');
      }
    });

    // Erreurs
    const errorSubscription = this.dashboardService.error$.subscribe(error => {
      if (error) {
        this.errorMessage.set(error);
        if (error.includes('Impossible de contacter le serveur')) {
          this.connectionStatus.set('disconnected');
        }
      }
    });

    // Auto-refresh
    const autoRefreshSubscription = this.dashboardService.autoRefreshEnabled$.subscribe(enabled => {
      this.autoRefreshEnabled.set(enabled);
    });

    this.subscriptions.push(dataSubscription, loadingSubscription, errorSubscription, autoRefreshSubscription);
  }

  /**
   * Rafraîchir manuellement les données
   */
  public refreshData(): void {
    if (this.connectionStatus() === 'disconnected') {
      console.log('🔄 Tentative de reconnexion au backend...');
      this.testBackendConnection();
      return;
    }

    console.log('🔄 Rafraîchissement manuel des données...');
    this.dashboardService.refreshData().subscribe({
      next: () => {
        this.showNotification('✅ Données rafraîchies avec succès !', 'success');
      },
      error: (error) => {
        this.showNotification('❌ Erreur lors du rafraîchissement', 'error');
        console.error('Erreur refresh:', error);
      }
    });
  }

  /**
   * Basculer le rafraîchissement automatique
   */
  public toggleAutoRefresh(): void {
    if (this.autoRefreshEnabled()) {
      this.dashboardService.pauseAutoRefresh();
      this.showNotification('⏸️ Actualisation automatique désactivée', 'warning');
    } else {
      this.dashboardService.resumeAutoRefresh();
      this.showNotification('▶️ Actualisation automatique reprise', 'success');
    }
  }

  /**
   * Forcer le mode hors-ligne
   */
  public switchToOfflineMode(): void {
    this.dashboardService.pauseAutoRefresh();
    this.cardData.set(this.mockCardData);
    this.globalStats.set(this.mockGlobalStats);
    this.connectionStatus.set('disconnected');
    this.errorMessage.set('Mode hors-ligne activé manuellement');
    this.lastUpdateTime.set(new Date().toLocaleString('fr-FR'));
    this.showNotification('📴 Passage en mode hors-ligne', 'warning');
  }

  /**
   * Tenter une reconnexion
   */
  public retryConnection(): void {
    this.clearError();
    this.connectionStatus.set('connecting');
    this.showNotification('🔄 Tentative de reconnexion...', 'warning');
    this.testBackendConnection();
  }

  /**
   * Effacer le message d'erreur
   */
  public clearError(): void {
    this.errorMessage.set(null);
    this.dashboardService.clearError();
  }

  // ==================== ACTIONS MÉTIER ====================

  public onRegenerateFiles(): void {
    console.log('🔧 Régénération des fichiers...');
    this.isLoading.set(true);
    setTimeout(() => {
      this.isLoading.set(false);
      this.refreshData(); // Refresh after operation
      this.showNotification('✅ Fichiers régénérés avec succès !', 'success');
    }, 3000);
  }

  public onValidateData(): void {
    console.log('✅ Validation des données...');
    this.isLoading.set(true);
    setTimeout(() => {
      this.isLoading.set(false);
      this.refreshData(); // Refresh after operation
      this.showNotification('✅ Données validées avec succès !', 'success');
    }, 2000);
  }

  public onExportData(): void {
    console.log('📤 Export des données...');
    
    const currentData = this.dashboardService.getCurrentData() || {
      cardData: this.cardData(),
      globalStats: this.globalStats()
    };

    const exportData = {
      timestamp: new Date().toISOString(),
      cardData: currentData.cardData,
      globalStats: currentData.globalStats,
      totalAmount: this.totalAmount(),
      lastUpdate: this.lastUpdateTime(),
      connectionStatus: this.connectionStatus(),
      source: this.connectionStatus() === 'connected' ? 'backend-spring-boot' : 'mock-data',
      version: '2.1',
      application: 'RUYA-Dashboard'
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ruya-dashboard-export-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);

    this.showNotification('📥 Données exportées avec succès !', 'success');
  }

  public onCloseApp(): void {
    if (confirm('🚪 Êtes-vous sûr de vouloir fermer l\'application RUYA ?')) {
      console.log('🛑 Fermeture de l\'application RUYA...');
      this.sessionActive.set(false);
      this.dashboardService.pauseAutoRefresh();
      setTimeout(() => {
        console.log('✅ Application RUYA fermée proprement');
      }, 500);
    }
  }

  // ==================== UTILITAIRES ====================

  /**
   * Simuler des changements de données (mode hors-ligne uniquement)
   */
  public updateRandomData(): void {
    if (this.connectionStatus() === 'disconnected') {
      console.log('🎲 Simulation de changements de données...');
      const currentData = this.cardData();
      const updatedData = currentData.map(card => ({
        ...card,
        data: card.data.map(row => {
          if (typeof row.value === 'number' && Math.random() > 0.95) {
            const change = Math.floor(Math.random() * 6) - 3;
            const newValue = Math.max(0, row.value + change);
            return { ...row, value: newValue };
          }
          return row;
        })
      }));
      this.cardData.set(updatedData);
      this.lastUpdateTime.set(new Date().toLocaleString('fr-FR'));
      this.showNotification('🔄 Données simulées mises à jour', 'success');
    }
  }

  private showNotification(message: string, type: 'success' | 'warning' | 'error' = 'success'): void {
    console.log(`${type.toUpperCase()}: ${message}`);
    // Ici vous pouvez intégrer une bibliothèque de notifications comme ngx-toastr
    // Pour l'instant, on log juste dans la console
  }

  // ==================== TEMPLATE HELPERS ====================

  public trackByIndex(index: number): number {
    return index;
  }

  public getStatusClass(status?: string): string {
    switch (status) {
      case 'success': return 'status-success';
      case 'warning': return 'status-warning';
      case 'danger': return 'status-danger';
      default: return '';
    }
  }

  public getCardClass(type: string): string {
    switch (type) {
      case 'primary': return 'card-primary';
      case 'success': return 'card-success';
      case 'warning': return 'card-warning';
      default: return '';
    }
  }

  public getConnectionStatusClass(): string {
    switch (this.connectionStatus()) {
      case 'connected': return 'status-success';
      case 'connecting': return 'status-warning';
      case 'disconnected': return 'status-danger';
      default: return '';
    }
  }

  // ==================== COMPUTED PROPERTIES ====================

  public hasError(): boolean {
    return this.errorMessage() !== null;
  }

  public hasData(): boolean {
    return this.cardData().length > 0;
  }

  public isBackendConnected(): boolean {
    return this.connectionStatus() === 'connected';
  }

  public getErrorCount(): number {
    return this.dashboardService.getConsecutiveErrorCount();
  }

  public getDataSource(): string {
    return this.isBackendConnected() ? 'Backend Spring Boot' : 'Données de démonstration';
  }
}
