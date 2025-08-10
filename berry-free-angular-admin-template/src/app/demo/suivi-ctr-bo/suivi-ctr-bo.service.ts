import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError, BehaviorSubject, interval } from 'rxjs';
import { map, catchError, timeout, retry, switchMap } from 'rxjs/operators';
import { getConfig, SuiviCtrBoConfig } from './suivi-ctr-bo.config';

// Interfaces basées sur vos vraies tables
export interface CTRBOData {
  numero: string;
  montant: number;
  nombre?: number;
  date: Date;
  type?: string;
  agence?: string;
  description?: string;
  natureFichier?: string;
  nomFichier?: string;
  sens?: string;
  typeFichier?: string;
}

export interface ComparaisonResult {
  numero: string;
  montant: number;
  statut: 'egal' | 'different' | 'manquant';
  details: string;
}

export interface Statistiques {
  totalEnvoye: number;
  totalRecu: number;
  totalEgal: number;
  totalDifferent: number;
  totalManquant: number;
}

@Injectable({
  providedIn: 'root'
})
export class SuiviCtrBoService {
  private config: SuiviCtrBoConfig;
  
  // Observables pour les données dynamiques
  private carthagoDataSubject = new BehaviorSubject<CTRBOData[]>([]);
  private localDataSubject = new BehaviorSubject<CTRBOData[]>([]);
  private comparaisonSubject = new BehaviorSubject<ComparaisonResult[]>([]);
  private statistiquesSubject = new BehaviorSubject<Statistiques | null>(null);

  // Observables publics
  public carthagoData$ = this.carthagoDataSubject.asObservable();
  public localData$ = this.localDataSubject.asObservable();
  public comparaison$ = this.comparaisonSubject.asObservable();
  public statistiques$ = this.statistiquesSubject.asObservable();

  constructor(private http: HttpClient) {
    this.config = getConfig();
    
    // Initialiser les données au démarrage
    this.initialiserDonnees();
    
    // Démarrer la surveillance automatique
    this.demarrerSurveillanceAutomatique();
    
    console.log('🚀 SuiviCtrBoService initialisé avec succès');
  }

  // Initialiser les données
  private initialiserDonnees(): void {
    const carthagoData = this.genererDonneesCarthago();
    const localData = this.genererDonneesLocale();
    
    this.carthagoDataSubject.next(carthagoData);
    this.localDataSubject.next(localData);
    
    // Calculer la comparaison initiale
    this.mettreAJourComparaison();
  }

  // Démarrer la surveillance automatique
  private demarrerSurveillanceAutomatique(): void {
    // Vérifier les nouvelles données toutes les 30 secondes
    interval(30000).pipe(
      switchMap(() => this.verifierNouvellesDonnees())
    ).subscribe({
      next: (nouvellesDonnees) => {
        if (nouvellesDonnees.carthago.length > 0 || nouvellesDonnees.locale.length > 0) {
          console.log('Nouvelles données détectées:', nouvellesDonnees);
          this.mettreAJourDonnees(nouvellesDonnees);
        }
      },
      error: (error) => console.error('Erreur surveillance automatique:', error)
    });
  }

  // Vérifier les nouvelles données
  private verifierNouvellesDonnees(): Observable<{carthago: CTRBOData[], locale: CTRBOData[]}> {
    return this.http.get<{carthago: CTRBOData[], locale: CTRBOData[]}>(`${this.config.localApiUrl}/nouvelles-donnees`).pipe(
      timeout(this.config.timeout),
      retry(this.config.retryAttempts),
      catchError(() => of({carthago: [], locale: []}))
    );
  }

  // Mettre à jour les données
  private mettreAJourDonnees(nouvellesDonnees: {carthago: CTRBOData[], locale: CTRBOData[]}): void {
    if (nouvellesDonnees.carthago.length > 0) {
      const carthagoActuel = this.carthagoDataSubject.value;
      const carthagoMisAJour = [...carthagoActuel, ...nouvellesDonnees.carthago];
      this.carthagoDataSubject.next(carthagoMisAJour);
    }

    if (nouvellesDonnees.locale.length > 0) {
      const localActuel = this.localDataSubject.value;
      const localMisAJour = [...localActuel, ...nouvellesDonnees.locale];
      this.localDataSubject.next(localMisAJour);
    }

    this.mettreAJourComparaison();
  }

  // Mettre à jour la comparaison
  private mettreAJourComparaison(): void {
    const carthagoData = this.carthagoDataSubject.value;
    const localData = this.localDataSubject.value;
    
    console.log('🔄 Mise à jour de la comparaison...');
    console.log('📊 Données Carthago:', carthagoData.length, 'fichiers');
    console.log('📊 Données Locales:', localData.length, 'fichiers');
    
    const resultats = this.comparerDonnees(carthagoData, localData);
    const statistiques = this.calculerStatistiques(resultats);
    
    this.comparaisonSubject.next(resultats);
    this.statistiquesSubject.next(statistiques);
    
    console.log('✅ Comparaison mise à jour avec', resultats.length, 'résultats');
    console.log('📈 Statistiques mises à jour:', statistiques);
  }

  // Ajouter un fichier dynamiquement
  ajouterFichier(nouveauFichier: CTRBOData): Observable<boolean> {
    return new Observable(observer => {
      try {
        // Simuler l'ajout en base de données
        console.log('Ajout du fichier:', nouveauFichier);
        
        // Ajouter aux données locales
        const localActuel = this.localDataSubject.value;
        const localMisAJour = [...localActuel, nouveauFichier];
        this.localDataSubject.next(localMisAJour);
        
        // Mettre à jour la comparaison
        this.mettreAJourComparaison();
        
        // Simuler la sauvegarde en base
        this.sauvegarderEnBase(nouveauFichier).subscribe({
          next: () => {
            console.log('Fichier sauvegardé avec succès');
            observer.next(true);
            observer.complete();
          },
          error: (error) => {
            console.error('Erreur sauvegarde:', error);
            observer.error(error);
          }
        });
        
      } catch (error) {
        observer.error(error);
      }
    });
  }

  // Recevoir un fichier de Carthago
  recevoirFichierCarthago(nouveauFichier: CTRBOData): Observable<boolean> {
    return new Observable(observer => {
      try {
        console.log('Réception fichier Carthago:', nouveauFichier);
        
        // Ajouter aux données Carthago
        const carthagoActuel = this.carthagoDataSubject.value;
        const carthagoMisAJour = [...carthagoActuel, nouveauFichier];
        this.carthagoDataSubject.next(carthagoMisAJour);
        
        // Mettre à jour la comparaison
        this.mettreAJourComparaison();
        
        observer.next(true);
        observer.complete();
        
      } catch (error) {
        observer.error(error);
      }
    });
  }

  // Sauvegarder en base de données
  private sauvegarderEnBase(fichier: CTRBOData): Observable<any> {
    return this.http.post(`${this.config.localApiUrl}/fichiers`, fichier).pipe(
      timeout(this.config.timeout),
      retry(this.config.retryAttempts),
      catchError(this.handleError)
    );
  }

  // Simulated API calls with timeout and retry
  getDonneesCarthago(): Observable<CTRBOData[]> {
    return this.carthagoData$;
  }

  getDonneesLocale(): Observable<CTRBOData[]> {
    return this.localData$;
  }

  // Simulated API calls with real data structure
  private simulerAppelCarthago(): Observable<CTRBOData[]> {
    // Simulate network delay
    return of(this.genererDonneesCarthago()).pipe(
      map(data => {
        console.log('Données Carthago récupérées:', data);
        return data;
      })
    );
  }

  private simulerAppelLocal(): Observable<CTRBOData[]> {
    // Simulate network delay
    return of(this.genererDonneesLocale()).pipe(
      map(data => {
        console.log('Données Locale récupérées:', data);
        return data;
      })
    );
  }

  // Generate Carthago data based on your table structure
  private genererDonneesCarthago(): CTRBOData[] {
    const baseDate = new Date('2024-07-26');
    
    return [
      {
        numero: '56',
        montant: 1222,
        nombre: 15,
        date: new Date(baseDate.getTime() + 10 * 60 * 60 * 1000), // 10:30
        type: 'CTR',
        agence: 'AG001',
        description: 'Chèque très rapide - Agence Centrale',
        natureFichier: 'rcp',
        nomFichier: 'fichier_cheque_30',
        sens: 'recu',
        typeFichier: 'cheque'
      },
      {
        numero: '55',
        montant: 14,
        nombre: 8,
        date: new Date(baseDate.getTime() + 11 * 60 * 60 * 1000 + 15 * 60 * 1000), // 11:15
        type: 'BO',
        agence: 'AG002',
        description: 'Bordereau d\'opération - Agence Nord',
        natureFichier: 'env',
        nomFichier: 'fichier_effet_40',
        sens: 'recu',
        typeFichier: 'effet'
      },
      {
        numero: '54',
        montant: 150000,
        nombre: 25,
        date: new Date(baseDate.getTime() + 12 * 60 * 60 * 1000), // 12:00
        type: 'CTR',
        agence: 'AG001',
        description: 'Chèque très rapide - Agence Centrale',
        natureFichier: 'env',
        nomFichier: 'fichier_cheque_30',
        sens: 'recu',
        typeFichier: 'cheque'
      },
      {
        numero: '53',
        montant: 10000,
        nombre: 12,
        date: new Date(baseDate.getTime() + 13 * 60 * 60 * 1000), // 13:00
        type: 'BO',
        agence: 'AG003',
        description: 'Bordereau d\'opération - Agence Sud',
        natureFichier: 'rcp',
        nomFichier: 'fichier_v_33',
        sens: 'emis',
        typeFichier: 'virement'
      },
      {
        numero: '52',
        montant: 50,
        nombre: 5,
        date: new Date(baseDate.getTime() + 14 * 60 * 60 * 1000), // 14:00
        type: 'CTR',
        agence: 'AG002',
        description: 'Chèque très rapide - Agence Nord',
        natureFichier: 'env',
        nomFichier: 'fichier0003',
        sens: 'emis',
        typeFichier: 'cheque'
      },
      {
        numero: '21', // NOUVEAU FICHIER AJOUTÉ DANS CARTHAGO
        montant: 41,
        nombre: 23,
        date: new Date('2025-07-26T18:34:32.576766'),
        type: 'BO',
        agence: 'AG001',
        description: 'Fichier effet ajouté via image',
        natureFichier: 'env',
        nomFichier: 'fichier_effet_4111',
        sens: 'recu',
        typeFichier: 'effet'
      }
    ];
  }

  // Generate Local data based on your table structure
  private genererDonneesLocale(): CTRBOData[] {
    // Return identical data to Carthago for full coherence
    return this.genererDonneesCarthago();
  }

  // Core comparison logic
  comparerDonnees(carthagoData: CTRBOData[], localeData: CTRBOData[]): ComparaisonResult[] {
    const resultats: ComparaisonResult[] = [];
    const carthagoMap = new Map<string, CTRBOData>();
    const localeMap = new Map<string, CTRBOData>();

    // Create maps for efficient lookup
    carthagoData.forEach(item => carthagoMap.set(item.numero, item));
    localeData.forEach(item => localeMap.set(item.numero, item));

    // Check all Carthago items
    carthagoData.forEach(carthagoItem => {
      const localeItem = localeMap.get(carthagoItem.numero);
      
      if (!localeItem) {
        // Item exists in Carthago but not in local
        resultats.push({
          numero: carthagoItem.numero,
          montant: carthagoItem.montant,
          statut: 'manquant',
          details: `Présent dans Carthago (${carthagoItem.nomFichier}), manquant en local`
        });
      } else if (Math.abs(carthagoItem.montant - localeItem.montant) > 0.01) {
        // Amounts are different
        resultats.push({
          numero: carthagoItem.numero,
          montant: carthagoItem.montant,
          statut: 'different',
          details: `Montant différent: Carthago=${carthagoItem.montant} (${carthagoItem.nomFichier}), Local=${localeItem.montant} (${localeItem.nomFichier})`
        });
      } else {
        // Items are equal
        resultats.push({
          numero: carthagoItem.numero,
          montant: carthagoItem.montant,
          statut: 'egal',
          details: `Montants identiques: ${carthagoItem.nomFichier} = ${localeItem.nomFichier}`
        });
      }
    });

    // Check for items in local but not in Carthago
    localeData.forEach(localeItem => {
      if (!carthagoMap.has(localeItem.numero)) {
        resultats.push({
          numero: localeItem.numero,
          montant: localeItem.montant,
          statut: 'manquant',
          details: `Présent en local (${localeItem.nomFichier}), manquant dans Carthago`
        });
      }
    });

    return resultats;
  }

  // Calculate statistics
  calculerStatistiques(resultats: ComparaisonResult[]): Statistiques {
    const carthagoData = this.carthagoDataSubject.value;
    const localData = this.localDataSubject.value;
    
    const stats: Statistiques = {
      totalEnvoye: carthagoData.length,  // Nombre de fichiers dans Carthago
      totalRecu: localData.length,       // Nombre de fichiers dans Local
      totalEgal: 0,
      totalDifferent: 0,
      totalManquant: 0
    };

    // Calculer les statistiques basées sur les résultats de comparaison
    resultats.forEach(resultat => {
      switch (resultat.statut) {
        case 'egal':
          stats.totalEgal++;
          break;
        case 'different':
          stats.totalDifferent++;
          break;
        case 'manquant':
          stats.totalManquant++;
          break;
      }
    });

    console.log('📊 Statistiques mises à jour:', stats);
    return stats;
  }

  // Export report
  exporterRapport(statistiques: Statistiques): void {
    const rapport = {
      date: new Date().toISOString(),
      statistiques: statistiques,
      message: 'Rapport de comparaison CTR/BO généré'
    };

    const blob = new Blob([JSON.stringify(rapport, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `rapport-ctr-bo-${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  // Connectivity check
  verifierConnectiviteCarthago(): Observable<boolean> {
    return this.http.get<boolean>(`${this.config.carthagoApiUrl}/health`).pipe(
      timeout(this.config.timeout),
      retry(this.config.retryAttempts),
      catchError(this.handleError)
    );
  }

  // Synchronization
  synchroniserAvecCarthago(): Observable<any> {
    return this.http.post(`${this.config.carthagoApiUrl}/sync`, {}).pipe(
      timeout(this.config.timeout),
      retry(this.config.retryAttempts),
      catchError(this.handleError)
    );
  }

  // Error reporting
  envoyerRapportErreur(erreur: any): Observable<any> {
    return this.http.post(`${this.config.localApiUrl}/errors`, {
      timestamp: new Date().toISOString(),
      error: erreur
    }).pipe(
      timeout(this.config.timeout),
      retry(this.config.retryAttempts),
      catchError(this.handleError)
    );
  }

  // Error handling
  private handleError(error: any): Observable<never> {
    console.error('Erreur dans SuiviCtrBoService:', error);
    return throwError(() => new Error('Une erreur est survenue lors de l\'opération'));
  }

  // Écouter les événements d'ajout de fichier depuis le formulaire externe
  ecouterAjoutFichier(): Observable<CTRBOData> {
    return new Observable(observer => {
      // Vérifier localStorage toutes les 2 secondes pour détecter les nouveaux fichiers
      const interval = setInterval(() => {
        const nouveauxFichiers = this.verifierNouveauxFichiers();
        if (nouveauxFichiers.length > 0) {
          nouveauxFichiers.forEach(fichier => {
            console.log('Nouveau fichier détecté:', fichier);
            observer.next(fichier);
          });
        }
      }, 2000);

      // Retourner une fonction de nettoyage
      return () => {
        clearInterval(interval);
      };
    });
  }

  // Vérifier les nouveaux fichiers dans localStorage
  private verifierNouveauxFichiers(): CTRBOData[] {
    try {
      const fichiersAjoutes = localStorage.getItem('fichiersAjoutes');
      if (fichiersAjoutes) {
        const fichiers = JSON.parse(fichiersAjoutes);
        const fichiersTraites = localStorage.getItem('fichiersTraites') || '[]';
        const traites = JSON.parse(fichiersTraites);
        
        const nouveaux = fichiers.filter((f: any) => !traites.includes(f.numero));
        
        if (nouveaux.length > 0) {
          // Marquer comme traités
          const tousTraites = [...traites, ...nouveaux.map((f: any) => f.numero)];
          localStorage.setItem('fichiersTraites', JSON.stringify(tousTraites));
          
          return nouveaux.map((f: any) => ({
            numero: f.numero || f.codeFichier || f.idFichier,
            montant: f.montant || 0,
            nombre: f.nomber || f.nombre || 0,
            date: new Date(),
            type: f.typeFichier === 'cheque' ? 'CTR' : 'BO',
            agence: 'AG001',
            description: `Fichier ajouté: ${f.nomFichier}`,
            natureFichier: f.sens === 'emis' ? 'env' : 'rcp',
            nomFichier: f.nomFichier || `fichier_${Date.now()}.env`,
            sens: f.sens || 'emis',
            typeFichier: f.typeFichier || 'cheque'
          }));
        }
      }
    } catch (error) {
      console.error('Erreur lors de la vérification des nouveaux fichiers:', error);
    }
    
    return [];
  }

  // Méthode pour déclencher l'ajout de fichier depuis le formulaire externe
  declencherAjoutFichier(fichierData: any): void {
    console.log('Déclenchement ajout fichier:', fichierData);
    
    try {
      // Sauvegarder dans localStorage
      const fichiersExistants = localStorage.getItem('fichiersAjoutes') || '[]';
      const fichiers = JSON.parse(fichiersExistants);
      fichiers.push(fichierData);
      localStorage.setItem('fichiersAjoutes', JSON.stringify(fichiers));
      
      console.log('Fichier sauvegardé dans localStorage:', fichierData);
    } catch (error) {
      console.error('Erreur lors de la sauvegarde du fichier:', error);
    }
  }

  // Méthode pour ajouter un fichier directement (pour test)
  ajouterFichierDirect(fichierData: any): void {
    console.log('🔄 Début ajouterFichierDirect avec données:', fichierData);
    
    const nouveauFichier: CTRBOData = {
      numero: fichierData.codeFichier || fichierData.idFichier || (Math.floor(Math.random() * 1000) + 100).toString(),
      montant: fichierData.montant || 0,
      nombre: fichierData.nomber || fichierData.nombre || 0,
      date: new Date(),
      type: fichierData.typeFichier === 'cheque' ? 'CTR' : 'BO',
      agence: 'AG001',
      description: `Fichier ajouté: ${fichierData.nomFichier}`,
      natureFichier: fichierData.sens === 'emis' ? 'env' : 'rcp',
      nomFichier: fichierData.nomFichier || `fichier_${Date.now()}.env`,
      sens: fichierData.sens || 'emis',
      typeFichier: fichierData.typeFichier || 'cheque'
    };

    console.log('📝 Nouveau fichier créé:', nouveauFichier);

    // Ajouter directement aux données locales
    const localActuel = this.localDataSubject.value;
    console.log('📊 Données locales actuelles:', localActuel.length, 'fichiers');
    
    const localMisAJour = [...localActuel, nouveauFichier];
    this.localDataSubject.next(localMisAJour);
    
    console.log('✅ Fichier ajouté aux données locales:', nouveauFichier.numero);
    console.log('📈 Nombre total de fichiers locaux:', localMisAJour.length);
    
    // Mettre à jour la comparaison et les statistiques
    this.mettreAJourComparaison();
    
    // Afficher les nouvelles statistiques
    const carthagoData = this.carthagoDataSubject.value;
    const stats = {
      totalEnvoye: carthagoData.length,
      totalRecu: localMisAJour.length,
      nouveauFichier: nouveauFichier.numero
    };
    
    console.log('📊 Nouvelles statistiques après ajout:', stats);
    console.log('🎯 Fichier ajouté avec succès dans +suivi CTR/BO');
  }

  // Traiter l'ajout de fichier depuis le formulaire externe
  traiterAjoutFichierDepuisFormulaire(fichierData: any): Observable<boolean> {
    return new Observable(observer => {
      try {
        console.log('Traitement du fichier depuis formulaire externe:', fichierData);
        
        const nouveauFichier: CTRBOData = {
          numero: fichierData.codeFichier || fichierData.idFichier || (Math.floor(Math.random() * 1000) + 100).toString(),
          montant: fichierData.montant || 0,
          nombre: fichierData.nomber || fichierData.nombre || 0,
          date: new Date(),
          type: fichierData.typeFichier === 'cheque' ? 'CTR' : 'BO',
          agence: 'AG001',
          description: `Fichier ajouté: ${fichierData.nomFichier}`,
          natureFichier: fichierData.sens === 'emis' ? 'env' : 'rcp',
          nomFichier: fichierData.nomFichier || `fichier_${Date.now()}.env`,
          sens: fichierData.sens || 'emis',
          typeFichier: fichierData.typeFichier || 'cheque'
        };

        // Ajouter aux données locales
        const localActuel = this.localDataSubject.value;
        const localMisAJour = [...localActuel, nouveauFichier];
        this.localDataSubject.next(localMisAJour);
        
        // Mettre à jour la comparaison
        this.mettreAJourComparaison();
        
        // Sauvegarder en base
        this.sauvegarderEnBase(nouveauFichier).subscribe({
          next: () => {
            console.log('Fichier sauvegardé avec succès depuis formulaire externe');
            observer.next(true);
            observer.complete();
          },
          error: (error) => {
            console.error('Erreur sauvegarde depuis formulaire externe:', error);
            observer.error(error);
          }
        });
        
      } catch (error) {
        observer.error(error);
      }
    });
  }

  // Méthode pour simuler l'ajout de fichier (pour test)
  simulerAjoutFichier(): void {
    const fichierTest = {
      codeFichier: (Math.floor(Math.random() * 1000) + 100).toString(),
      montant: Math.floor(Math.random() * 50000) + 1000,
      nomber: Math.floor(Math.random() * 50) + 10,
      typeFichier: Math.random() > 0.5 ? 'cheque' : 'virement',
      sens: Math.random() > 0.5 ? 'emis' : 'recu',
      nomFichier: `fichier_test_${Date.now()}.env`
    };
    
    this.declencherAjoutFichier(fichierTest);
  }

  // Charger les fichiers depuis la base de données
  chargerFichiersDepuisBase(): Observable<CTRBOData[]> {
    const baseUrl = 'http://localhost:8081/api/fichiers';
    
    return this.http.get<any[]>(baseUrl).pipe(
      map(fichiers => {
        console.log('📊 Fichiers récupérés depuis la base:', fichiers.length);
        
        // Convertir les fichiers de la base vers le format CTRBOData
        const fichiersConvertis: CTRBOData[] = fichiers.map(fichier => ({
          numero: fichier.codeValeur || fichier.codeFichier || fichier.id?.toString(),
          montant: parseFloat(fichier.montant) || 0,
          nombre: parseInt(fichier.nomber) || 0,
          date: new Date(fichier.dateCreation || fichier.createdAt || Date.now()),
          type: fichier.typeFichier === 'cheque' ? 'CTR' : 'BO',
          agence: 'AG001',
          description: `Fichier de la base: ${fichier.nomFichier}`,
          natureFichier: fichier.natureFichier || 'env',
          nomFichier: fichier.nomFichier || `fichier_${fichier.id}.env`,
          sens: fichier.sens || 'emis',
          typeFichier: fichier.typeFichier || 'cheque'
        }));
        
        console.log('✅ Fichiers convertis pour +suivi CTR/BO:', fichiersConvertis.length);
        
        // Mettre à jour les données locales
        this.localDataSubject.next(fichiersConvertis);
        
        // Mettre à jour la comparaison
        this.mettreAJourComparaison();
        
        return fichiersConvertis;
      }),
      catchError(error => {
        console.error('❌ Erreur lors du chargement depuis la base:', error);
        return of([]);
      })
    );
  }
} 