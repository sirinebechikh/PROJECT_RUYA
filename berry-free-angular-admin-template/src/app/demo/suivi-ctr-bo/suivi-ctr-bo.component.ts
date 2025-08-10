import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { SuiviCtrBoService } from './suivi-ctr-bo.service';
import { ApiService } from '../../services/api.service';
import { Subscription } from 'rxjs';

interface FichierData {
  id: number;
  nomFichier: string;
  typeFichier: string;
  natureFichier: string;
  codeValeur: string;
  codEn: string;
  sens: string;
  montant: number;
  nomber: number;
  createdAt: string;
  updatedAt: string;
  statut?: string;
  user: {
    id: number;
    username: string;
    email: string;
  };
}

@Component({
  selector: 'app-suivi-ctr-bo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule],
  providers: [SuiviCtrBoService],
  templateUrl: './suivi-ctr-bo.component.html',
  styleUrls: ['./suivi-ctr-bo.component.scss']
})
export class SuiviCtrBoComponent implements OnInit, OnDestroy {
  suiviForm: FormGroup;
  isLoading = false;
  isOnline = true;
  
  // Données simplifiées
  totalFichiersCrees = 0;
  totalFichiersEnvoyes = 0;
  totalFichiersValides = 0;
  
  totalFichiers = 0;
  fichiersValides = 0;
  fichiersEnAttente = 0;
  fichiersErreurs = 0;
  
  fichiersListe: FichierData[] = [];
  fichierSelectionne: FichierData | null = null;
  
  // Utilisateur connecté pour le filtrage de sécurité
  currentUserId: number | null = null;
  currentUsername: string = '';
  
  private subscriptions: Subscription[] = [];

  constructor(
    private fb: FormBuilder,
    private suiviCtrBoService: SuiviCtrBoService,
    private apiService: ApiService
  ) {
    this.suiviForm = this.fb.group({
      date: [''],
      statut: ['']
    });
  }

  ngOnInit(): void {
    console.log('🔄 Initialisation du composant +suivi CTR/BO');
    
    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    this.suiviForm.patchValue({ date: today });

    // Charger les données initiales
    this.chargerDonnees();
    
    // Vérifier la connectivité
    this.verifierConnectivite();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    console.log('🧹 Composant +suivi CTR/BO détruit');
  }

  // Charger toutes les données
  chargerDonnees(): void {
    console.log('📊 Chargement des données');
    this.isLoading = true;

    // Charger les fichiers depuis la base de données
    this.subscriptions.push(
      this.apiService.getAllFichiers().subscribe({
        next: (fichiers) => {
          console.log('✅ Fichiers chargés:', fichiers.length);
          this.traiterFichiers(fichiers);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('❌ Erreur chargement fichiers:', error);
          this.isLoading = false;
        }
      })
    );
  }

  // Traiter les fichiers reçus
  traiterFichiers(fichiers: any[]): void {
    // Convertir et ajouter le statut
    this.fichiersListe = fichiers.map(fichier => ({
      ...fichier,
      statut: this.getStatutFromCode(fichier.codeValeur)
    }));

    // Calculer les statistiques
    this.calculerStatistiques();
    
    console.log('✅ Fichiers traités:', this.fichiersListe.length);
  }

  // Obtenir le statut à partir du code
  getStatutFromCode(codeValeur: string): string {
    switch (codeValeur) {
      case '30':
        return 'REMIS';
      case '31':
        return 'REJET';
      case '32':
        return 'RENDU';
      case '33':
        return 'EN_ATTENTE';
      default:
        return 'INCONNU';
    }
  }

  // Calculer les statistiques
  calculerStatistiques(): void {
    this.totalFichiers = this.fichiersListe.length;
    
    // Compter par statut
    const stats = this.fichiersListe.reduce((acc, fichier) => {
      const statut = fichier.statut || 'INCONNU';
      acc[statut] = (acc[statut] || 0) + 1;
      return acc;
    }, {} as any);

    this.fichiersValides = stats['REMIS'] || 0;
    this.fichiersEnAttente = stats['EN_ATTENTE'] || 0;
    this.fichiersErreurs = (stats['REJET'] || 0) + (stats['RENDU'] || 0);

    // Calculer les étapes du processus
    this.totalFichiersCrees = this.totalFichiers;
    this.totalFichiersEnvoyes = this.totalFichiers - this.fichiersEnAttente;
    this.totalFichiersValides = this.fichiersValides;

    console.log('📊 Statistiques calculées:', {
      total: this.totalFichiers,
      valides: this.fichiersValides,
      enAttente: this.fichiersEnAttente,
      erreurs: this.fichiersErreurs
    });
  }

  // Rechercher des fichiers
  rechercherFichiers(): void {
    if (this.suiviForm.valid) {
      console.log('🔍 Recherche fichiers:', this.suiviForm.value);
      this.isLoading = true;

      const params = this.suiviForm.value;
      
      this.subscriptions.push(
        this.apiService.getFichiersWithFilters(params).subscribe({
          next: (fichiers) => {
            console.log('✅ Résultats recherche:', fichiers.length);
            this.traiterFichiers(fichiers);
            this.isLoading = false;
          },
          error: (error) => {
            console.error('❌ Erreur recherche:', error);
            this.isLoading = false;
          }
        })
      );
    }
  }

  // Voir les détails d'un fichier
  voirDetails(fichier: FichierData): void {
    console.log('👁️ Voir détails:', fichier);
    this.fichierSelectionne = fichier;
  }

  // Vérifier la connectivité
  verifierConnectivite(): void {
    console.log('🔌 Vérification connectivité');
    
    this.subscriptions.push(
      this.apiService.getStatsByStatus().subscribe({
        next: () => {
          this.isOnline = true;
          console.log('✅ Connecté à la base de données');
        },
        error: (error) => {
          this.isOnline = false;
          console.error('❌ Erreur de connectivité:', error);
        }
      })
    );
  }

  // Méthode pour rafraîchir les données
  rafraichirDonnees(): void {
    console.log('🔄 Rafraîchissement des données');
    this.chargerDonnees();
  }

  // Méthode pour exporter les données
  exporterDonnees(): void {
    console.log('📄 Export des données');
    
    const donnees = {
      date: new Date().toISOString(),
      statistiques: {
        totalFichiers: this.totalFichiers,
        fichiersValides: this.fichiersValides,
        fichiersEnAttente: this.fichiersEnAttente,
        fichiersErreurs: this.fichiersErreurs
      },
      fichiers: this.fichiersListe
    };

    const blob = new Blob([JSON.stringify(donnees, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `suivi-ctr-bo-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  // Méthode pour synchroniser avec Carthago
  synchroniserCarthago(): void {
    console.log('🔄 Synchronisation avec Carthago');
    
    this.subscriptions.push(
      this.suiviCtrBoService.synchroniserAvecCarthago().subscribe({
        next: (result) => {
          console.log('✅ Synchronisation réussie:', result);
          this.rafraichirDonnees();
        },
        error: (error) => {
          console.error('❌ Erreur de synchronisation:', error);
        }
      })
    );
  }
} 