# 📊 Guide d'Utilisation - Module +suivi CTR/BO

## 🎯 **Objectif**
Le module +suivi CTR/BO permet de **contrôler et valider** les fichiers financiers entre RU'ya et Carthago de manière simple et logique.

## 🔄 **Flux de Traitement Simple**

### **Étape 1 : Fichiers créés dans RU'ya**
- Les utilisateurs ajoutent des fichiers via le formulaire "Ajouter un fichier"
- Chaque fichier reçoit un code de statut (30, 31, 32, 33)

### **Étape 2 : Envoi vers Carthago**
- Les fichiers sont automatiquement transmis à Carthago
- Carthago traite et valide les données

### **Étape 3 : Validation CTR/BO**
- CTR/BO vérifie la cohérence des données
- Retourne les statuts mis à jour

## 📊 **Interface Utilisateur**

### **1. Vue d'ensemble**
```
📁 Fichiers créés → 📤 Envoyés vers Carthago → ✅ Validés par CTR/BO
```

### **2. Statistiques Principales**
- **Total Fichiers** : Nombre total de fichiers dans le système
- **Validés** : Fichiers avec statut "REMIS" (code 30)
- **En Attente** : Fichiers avec statut "EN_ATTENTE" (code 33)
- **Erreurs** : Fichiers avec statut "REJET" (code 31) ou "RENDU" (code 32)

### **3. Recherche Simple**
- **Date** : Filtrer par date de création
- **Statut** : Filtrer par statut (REMIS, REJET, RENDU, EN_ATTENTE)

### **4. Liste des Fichiers**
- **Nom du Fichier** : Nom du fichier
- **Type** : Cheque, Effet, Virement
- **Montant** : Montant en TND
- **Statut** : Badge coloré selon le statut
- **Date** : Date de création
- **Actions** : Bouton pour voir les détails

### **5. Détails du Fichier**
- **Informations Générales** : Nom, Type, Montant, Statut
- **Informations Techniques** : Code Valeur, Sens, Nombre, Date

## 🎨 **Codes de Statut**

| Code | Statut | Couleur | Signification |
|------|--------|---------|---------------|
| 30 | REMIS | 🟢 Vert | Fichier validé et traité |
| 31 | REJET | 🔴 Rouge | Fichier rejeté par CTR/BO |
| 32 | RENDU | 🟡 Jaune | Fichier rendu pour correction |
| 33 | EN_ATTENTE | 🔵 Bleu | Fichier en attente de traitement |

## 🔧 **Fonctionnalités**

### **✅ Liaison Base de Données**
- Connexion directe à la base Oracle
- Récupération en temps réel des données
- Mise à jour automatique des statistiques

### **✅ Recherche et Filtrage**
- Recherche par date
- Filtrage par statut
- Affichage en temps réel

### **✅ Détails Complets**
- Vue détaillée de chaque fichier
- Informations techniques complètes
- Historique des modifications

### **✅ Export de Données**
- Export JSON des données
- Rapport complet avec statistiques
- Nom de fichier avec date

### **✅ Statut de Connexion**
- Indicateur de connectivité
- Vérification base de données
- Vérification Carthago

## 🚀 **Utilisation**

### **1. Accéder au Module**
- Cliquer sur "+suivi CTR/BO" dans le menu
- Interface simple et intuitive

### **2. Consulter les Statistiques**
- Vue d'ensemble en haut de page
- Cartes statistiques avec couleurs
- Mise à jour automatique

### **3. Rechercher des Fichiers**
- Sélectionner une date
- Choisir un statut (optionnel)
- Cliquer sur "Rechercher"

### **4. Voir les Détails**
- Cliquer sur l'icône "œil" dans la liste
- Détails complets du fichier
- Informations techniques

### **5. Exporter les Données**
- Données automatiquement exportées
- Format JSON structuré
- Nom de fichier avec date

## 🔗 **Liaison avec le Dashboard**

### **Synchronisation Automatique**
- Les données sont partagées avec le dashboard principal
- Mise à jour en temps réel
- Statistiques cohérentes

### **Événements**
- Nouveaux fichiers ajoutés automatiquement
- Mise à jour des statistiques
- Synchronisation bidirectionnelle

## 📱 **Responsive Design**

### **Desktop**
- Affichage complet avec toutes les fonctionnalités
- Cartes côte à côte
- Tableaux détaillés

### **Mobile**
- Interface adaptée aux petits écrans
- Cartes empilées
- Navigation simplifiée

## 🎯 **Avantages**

### **✅ Simplicité**
- Interface claire et logique
- Navigation intuitive
- Informations essentielles en vue

### **✅ Efficacité**
- Liaison directe base de données
- Mise à jour automatique
- Recherche rapide

### **✅ Fiabilité**
- Vérification connectivité
- Gestion d'erreurs
- Données cohérentes

### **✅ Flexibilité**
- Filtrage multiple
- Export personnalisé
- Détails complets

## 🔧 **Configuration**

### **URLs API**
- **Local** : `http://localhost:8081/api`
- **Carthago** : `http://localhost:8082/api` (simulation)

### **Timeouts**
- **Requête** : 10 secondes
- **Rafraîchissement** : 30 secondes
- **Tentatives** : 3 fois

## 📞 **Support**

Pour toute question ou problème :
1. Vérifier la connectivité base de données
2. Consulter les logs de la console
3. Rafraîchir les données
4. Contacter l'équipe technique

---

**🎉 Module +suivi CTR/BO - Simple, Logique, Efficace !** 