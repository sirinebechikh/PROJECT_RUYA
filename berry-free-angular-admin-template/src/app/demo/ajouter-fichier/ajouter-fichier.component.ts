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

  nomFichier: string = '';
  typeFichier: string = '';
  formatFichier: string = '';
  codeFichier: string = '';
  codeEnregistrement: string = '';
  sens: string = '';
  montant: string = '';
  nombre: string = '';
  idUser: number = 1; // ID utilisateur par défaut

  typesFichier = [
    { value: 'cheque', label: 'Chèque', icon: 'ti ti-currency-dollar' },
    { value: 'effet', label: 'Effet', icon: 'ti ti-file-invoice' },
    { value: 'prelevement', label: 'Prélèvement', icon: 'ti ti-credit-card' },
    { value: 'virement', label: 'Virement', icon: 'ti ti-arrows-double-ne-sw' }
  ];

  codesFichier: { [key: string]: Array<{ value: string, label: string }> } = {
    cheque: [
      { value: '30', label: '30' },
      { value: '31', label: '31' },
      { value: '32', label: '32' },
      { value: '33', label: '33' }
    ],
    effet: [
      { value: '40', label: '40' },
      { value: '41', label: '41' }
    ],
    prelevement: [{ value: '20', label: '20' }],
    virement: [{ value: '10', label: '10' }]
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
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
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

  onSubmit() {
    // Validation côté client
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

    // Préparer les données
    const montantNum = parseFloat(this.montant);
    const nombreNum = parseInt(this.nombre);

    const fichierData = {
      nomFichier: this.nomFichier.trim(),
      typeFichier: this.typeFichier,
      natureFichier: this.formatFichier || 'standard',
      codeValeur: this.codeFichier || null,
      codEn: this.codeEnregistrement || null,
      sens: this.sens,
      montant: montantNum,
      nomber: nombreNum,
      user: { id: this.idUser }
    };

    console.log('📤 Envoi des données:', fichierData);

    // Utiliser le service API
    this.apiService.createFichier(fichierData).subscribe({
      next: (response) => {
        console.log('✅ Fichier créé avec succès:', response);
        alert('Fichier ajouté avec succès !');
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
  }

  closeModal() {
    this.ajouterFichierService.closeModal();
  }
}
