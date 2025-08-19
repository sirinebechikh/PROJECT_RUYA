 import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AjouterFichierService } from './ajouter-fichier.service';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-ajouter-fichier',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-fichier.component.html',
  styleUrls: ['./ajouter-fichier.component.scss']
})
export class AjouterFichierComponent implements OnInit, OnDestroy {
  isOpen: boolean = false;
  private subscription: Subscription = new Subscription();

  // Propriétés existantes
  nomFichier: string = '';
  typeFichier: string = '';
  formatFichier: string = '';
  codeFichier: string = '';
  codeEnregistrement: string = '';
  sens: string = '';
  montant: string = '';
  nombre: string = '';
  idUser: number = 1; // ID utilisateur par défaut

  // NOUVELLES PROPRIÉTÉS
  numeroRemise: string = '';
  validation: string = '';
  origineSaisie: string = 'WEB'; // Valeur par défaut
  typeEncaissement: string = '';

  typesFichier = [
    { value: 'cheque', label: 'Chèque', icon: 'ti ti-currency-dollar' },
    { value: 'effet', label: 'Effet', icon: 'ti ti-file-invoice' },
    { value: 'prelevement', label: 'Prélèvement', icon: 'ti ti-credit-card' },
    { value: 'virement', label: 'Virement', icon: 'ti ti-arrows-double-ne-sw' }
  ];

  codesFichier: { [key: string]: Array<{ value: string, label: string }> } = {
    cheque: [
      { value: '30', label: '30 - Remise cheque' },
      { value: '31', label: '31 - Cheque rejeté' },
      { value: '32', label: '32 - Cheque payé' },
      { value: '33', label: '33 - Cheque impayé' }
    ],
    effet: [
      { value: '40', label: '40 - Remise effet' },
      { value: '41', label: '41 - Effet rejeté' }
    ],
    prelevement: [{ value: '20', label: '20 - Prélèvement' }],
    virement: [{ value: '10', label: '10 - Virement' }]
  };

  sensOptions = [
    { value: 'emis', label: 'Émis', icon: 'ti ti-arrow-up' },
    { value: 'recu', label: 'Reçu', icon: 'ti ti-arrow-down' }
  ];

  codeEnregistrementOptions = [
    { value: '21', label: '21 - Présentation', icon: 'ti ti-check' },
    { value: '22', label: '22 - Rejet', icon: 'ti ti-x' }
  ];

  formatFichierOptions = [
    { value: 'env', label: '.ENV', icon: 'ti ti-file' },
    { value: 'rcp', label: '.RCP', icon: 'ti ti-file' }
  ];

  constructor(
    private ajouterFichierService: AjouterFichierService,
    private apiService: ApiService
  ) {}

  ngOnInit() {
    // Initialiser avec l'ID utilisateur depuis localStorage si disponible
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.idUser = user.id || 1;
      } catch (e) {
        console.warn('Erreur lors du parsing de l\'utilisateur:', e);
      }
    }

    // S'abonner à l'état du modal
    this.subscription = this.ajouterFichierService.isModalOpen$.subscribe(
      isOpen => this.isOpen = isOpen
    );

    // Générer automatiquement un numéro de remise
    this.genererNumeroRemise();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  /**
   * Génère automatiquement un numéro de remise au format REM-YYYY-XXX
   */
  genererNumeroRemise() {
    const annee = new Date().getFullYear();
    const numero = Math.floor(Math.random() * 999) + 1;
    this.numeroRemise = `REM-${annee}-${numero.toString().padStart(3, '0')}`;
  }

  getCodesDisponibles() {
    return this.codesFichier[this.typeFichier] || [];
  }

  onClose() {
    this.ajouterFichierService.closeModal();
  }

  onBackdropClick(event: Event) {
    if (event.target === event.currentTarget) {
      this.onClose();
    }
  }

  /**
   * Validation du numéro de remise
   */
  private validerNumeroRemise(): boolean {
    const regex = /^[A-Z]{2,4}-[0-9]{4}-[0-9]{3,6}$/;
    return regex.test(this.numeroRemise);
  }

  onSubmit() {
    // Validation côté client améliorée
    if (!this.nomFichier || this.nomFichier.trim().length < 3) {
      alert('Le nom du fichier doit contenir au moins 3 caractères.');
      return;
    }

    if (!this.typeFichier) {
      alert('Veuillez sélectionner un type de fichier.');
      return;
    }

    if (!this.sens) {
      alert('Veuillez sélectionner un sens.');
      return;
    }

    if (!this.montant || isNaN(Number(this.montant)) || Number(this.montant) <= 0) {
      alert('Veuillez saisir un montant valide.');
      return;
    }

    if (!this.nombre || isNaN(Number(this.nombre)) || Number(this.nombre) <= 0) {
      alert('Veuillez saisir un nombre valide.');
      return;
    }

    // NOUVELLE VALIDATION: Numéro de remise
    if (!this.numeroRemise || !this.validerNumeroRemise()) {
      alert('Veuillez saisir un numéro de remise valide (format: REM-YYYY-XXX).');
      return;
    }

    // Préparer les données avec les nouveaux champs
    const montantNum = parseFloat(this.montant);
    const nombreNum = parseInt(this.nombre);
    const validationBool = this.validation ? this.validation === 'true' : null;

    const fichierData = {
      nomFichier: this.nomFichier.trim(),
      typeFichier: this.typeFichier,
      natureFichier: this.formatFichier || 'standard',
      codeValeur: this.codeFichier || null,
      codEn: this.codeEnregistrement || null,
      sens: this.sens,
      montant: montantNum,
      nomber: nombreNum,
      user: { id: this.idUser },
      
      // NOUVEAUX CHAMPS
      numeroRemise: this.numeroRemise,
      validation: validationBool,
      dateValidation: validationBool === true ? new Date().toISOString() : null,
      origineSaisie: this.origineSaisie || 'WEB',
      typeEncaissement: this.typeEncaissement || null,
      genereParEncaisse: false // Par défaut à false pour les saisies manuelles
    };

    console.log('📤 Envoi des données avec nouveaux champs:', fichierData);

    // Utiliser le service API
    this.apiService.createFichier(fichierData).subscribe({
      next: (response) => {
        console.log('✅ Fichier créé avec succès:', response);
        alert(`Fichier ajouté avec succès !\nNuméro de remise: ${this.numeroRemise}`);
        this.resetForm();
        this.ajouterFichierService.closeModal();
      },
      error: (err) => {
        console.error('❌ Erreur lors de l\'ajout du fichier:', err);
        let errorMessage = 'Erreur lors de l\'ajout du fichier';
        if (err.error && typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.message) {
          errorMessage = err.message;
        }
        alert(errorMessage);
      }
    });
  }

  resetForm() {
    this.nomFichier = '';
    this.typeFichier = '';
    this.formatFichier = '';
    this.codeFichier = '';
    this.codeEnregistrement = '';
    this.sens = '';
    this.montant = '';
    this.nombre = '';
    
    // Réinitialiser les nouveaux champs
    this.validation = '';
    this.origineSaisie = 'WEB';
    this.typeEncaissement = '';
    
    // Générer un nouveau numéro de remise
    this.genererNumeroRemise();
  }

  closeModal() {
    this.ajouterFichierService.closeModal();
  }

  /**
   * Méthode utilitaire pour vérifier si le formulaire est valide
   */
  isFormValid(): boolean {
    return !!(
      this.nomFichier && 
      this.typeFichier && 
      this.sens && 
      this.montant && 
      this.nombre && 
      this.numeroRemise &&
      this.validerNumeroRemise()
    );
  }
}