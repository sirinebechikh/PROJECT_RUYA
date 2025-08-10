import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../authentication/auth.service';

export interface Utilisateur {
  id: number;           
  username: string;
  email: string;
  role: string;
  password: string;
  isActive: boolean;
  createdAt: Date;        
  updatedAt: Date;
}

@Component({
  selector: 'app-gestion-utilisateur',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-utilisateur.component.html',
  styleUrls: ['./gestion-utilisateur.component.scss']
})
export class GestionUtilisateurComponent implements OnInit {
  utilisateurs: Utilisateur[] = [];

  // Pour modal d'ajout : username au lieu de nom/prenom séparés
  newUser = { username: '', email: '', password: '' };
  isModalOpen = false;
  errorMsg = '';

  constructor(private userService: AuthService) {}

  ngOnInit() {
    this.userService.getAllUsers().subscribe({
      next: (users) => this.utilisateurs = users,
      error: (err) => console.error('Erreur lors du chargement des utilisateurs', err)
    });
  }

  supprimerUtilisateur(id: number) {
    this.utilisateurs = this.utilisateurs.filter(u => u.id !== id);
  }

  activerUtilisateur(id: number) {
    this.userService.updateUserStatus(id, true).subscribe({
      next: updatedUser => {
        const user = this.utilisateurs.find(u => u.id === id);
        if (user) user.isActive = updatedUser.isActive;
      },
      error: err => console.error('Erreur activation', err)
    });
  }

  desactiverUtilisateur(id: number) {
    this.userService.updateUserStatus(id, false).subscribe({
      next: updatedUser => {
        const user = this.utilisateurs.find(u => u.id === id);
        if (user) user.isActive = updatedUser.isActive;
      },
      error: err => console.error('Erreur désactivation', err)
    });
  }

  ouvrirModal() {
    this.isModalOpen = true;
    this.newUser = { username: '', email: '', password: '' };
    this.errorMsg = '';
  }

  fermerModal() {
    this.isModalOpen = false;
  }

ajouterUtilisateur() {
  this.errorMsg = '';

  if (!this.newUser.username || !this.newUser.email || !this.newUser.password) {
    this.errorMsg = 'Tous les champs sont obligatoires.';
    return;
  }
  if (!/^\S+@\S+\.\S+$/.test(this.newUser.email)) {
    this.errorMsg = 'Email invalide.';
    return;
  }

  const nouvelUtilisateur = {
    username: this.newUser.username,
    email: this.newUser.email,
    password: this.newUser.password,
  };

  this.userService.register(nouvelUtilisateur).subscribe({
    next: (createdUser) => {
      this.utilisateurs.push(createdUser);
      this.fermerModal();
    },
    error: (err) => {
      console.error('Erreur lors de l\'ajout utilisateur', err);
      this.errorMsg = 'Erreur lors de l\'ajout utilisateur';
    }
  });
}

  
}
