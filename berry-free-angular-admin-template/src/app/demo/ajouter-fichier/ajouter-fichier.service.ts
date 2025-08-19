import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { SuiviCtrBoService } from '../suivi-ctr-bo/suivi-ctr-bo.service';
import { CurrentUserService } from '../../services/current-user.service';

export interface FileEvent {
  type: 'ajout' | 'envoi' | 'reception';
  fichier: any;
  timestamp: Date;
  username?: string; // Nom de l'utilisateur qui a effectué l'action
}

@Injectable({
  providedIn: 'root'
})
export class AjouterFichierService {
  private isModalOpen = new BehaviorSubject<boolean>(false);
  isModalOpen$ = this.isModalOpen.asObservable();

  public baseUrl = 'http://localhost:8081/api/fichiers';

  // Observable de la liste des fichiers
  private fichiersSubject = new BehaviorSubject<any[]>([]);
  fichiers$ = this.fichiersSubject.asObservable();

  // Observable pour notifier ajout fichier
  fichierAjoute$ = new Subject<void>();

  // Observable pour les événements de fichiers
  private fileEventsSubject = new Subject<FileEvent>();
  fileEvents$ = this.fileEventsSubject.asObservable();

  constructor(
    private http: HttpClient,
    private suiviCtrBoService: SuiviCtrBoService,
    private currentUserService: CurrentUserService
  ) {}

  openModal() {
    this.isModalOpen.next(true);
  }

  closeModal() {
    this.isModalOpen.next(false);
  }

  getAllFichiers(): Observable<any[]> {
    try {
      const userId = this.currentUserService.requireUserId();
      console.log('📋 Récupération des fichiers pour l\'utilisateur:', this.currentUserService.getUserLogInfo());
      return this.http.get<any[]>(`${this.baseUrl}/getallbyuser/${userId}`);
    } catch (error) {
      console.error('❌ Erreur lors de la récupération des fichiers:', error);
      throw error;
    }
  }

  ajouterFichier(fichier: any): Observable<any> {
    console.log('🔄 Ajout de fichier en base de données:', fichier);
    
    return this.http.post<any>(this.baseUrl, fichier).pipe(
      tap((nouveauFichier) => {
        console.log('✅ Fichier sauvegardé en base:', nouveauFichier);
        console.log('🔍 DEBUG - Fichier créé avec utilisateur:', nouveauFichier.user);
        
        // Obtenir le nom d'utilisateur via le service
        const username = this.currentUserService.getCurrentUsername();
        
        console.log('🔍 DEBUG - Nom d\'utilisateur récupéré:', username);
        console.log('🔍 DEBUG - Info utilisateur:', this.currentUserService.getUserLogInfo());
        
        // Émettre un événement de notification avec le nom d'utilisateur
        const fileEvent: FileEvent = {
          type: 'ajout',
          fichier: nouveauFichier,
          timestamp: new Date(),
          username: username
        };
        
        console.log('🔍 DEBUG - Émission d\'événement de fichier:', fileEvent);
        console.log('🔍 DEBUG - Type d\'événement:', fileEvent.type);
        console.log('🔍 DEBUG - Fichier avec utilisateur:', fileEvent.fichier);
        console.log('🔍 DEBUG - Username dans l\'événement:', fileEvent.username);
        this.fileEventsSubject.next(fileEvent);
        
        // 🔗 LIAISON AVEC +SUIVI CTR/BO
        // Convertir les données pour le module de suivi
        const donneesPourSuivi = {
          codeFichier: fichier.codeValeur || fichier.codeFichier,
          montant: parseFloat(fichier.montant) || 0,
          nomber: parseInt(fichier.nomber) || 0,
          typeFichier: fichier.typeFichier,
          sens: fichier.sens,
          nomFichier: fichier.nomFichier,
          natureFichier: fichier.natureFichier,
          codeEnregistrement: fichier.codEn || fichier.codeEnregistrement
        };
        
        console.log('📊 Données converties pour +suivi CTR/BO:', donneesPourSuivi);
        
        try {
          // Ajouter au module +suivi CTR/BO
          this.suiviCtrBoService.ajouterFichierDirect(donneesPourSuivi);
          console.log('✅ Fichier ajouté avec succès dans +suivi CTR/BO');
        } catch (error) {
          console.error('❌ Erreur lors de l\'ajout dans +suivi CTR/BO:', error);
        }
        
        // Notifier l'ajout
        this.fichierAjoute$.next();
      })
    );
  }

  setFichiers(fichiers: any[]) {
    this.fichiersSubject.next(fichiers);
  }

  // Ajoute la modification d'un fichier
  modifierFichier(fichier: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${fichier.id}`, fichier);
  }

  // Ajoute la suppression d'un fichier
  supprimerFichier(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`);
  }

  // Méthode pour émettre un événement d'envoi de fichier
  emettreEnvoiFichier(fichier: any) {
    // Obtenir le nom d'utilisateur via le service
    const username = this.currentUserService.getCurrentUsername();
    
    const fileEvent: FileEvent = {
      type: 'envoi',
      fichier: fichier,
      timestamp: new Date(),
      username: username
    };
    
    this.fileEventsSubject.next(fileEvent);
  }

  // Méthode pour émettre un événement de réception de fichier
  emettreReceptionFichier(fichier: any) {
    // Obtenir le nom d'utilisateur via le service
    const username = this.currentUserService.getCurrentUsername();
    
    const fileEvent: FileEvent = {
      type: 'reception',
      fichier: fichier,
      timestamp: new Date(),
      username: username
    };
    
    this.fileEventsSubject.next(fileEvent);
  }
}
