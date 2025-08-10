# RU'ya - Application de Gestion de Fichiers Financiers

## 🎯 Objectif
Application web moderne et interactive pour visualiser, analyser et suivre les fichiers métiers contenant des chèques ou des effets. Chaque fichier regroupe plusieurs opérations financières et contient toutes les informations nécessaires pour analyser les flux entrants et sortants.

## 🏗️ Architecture

### Backend (Spring Boot)
- **Port**: 8081
- **Base de données**: Oracle
- **API REST**: `/api/fichiers/*`
- **Configuration CORS**: Autorise les requêtes depuis le frontend

### Frontend (Angular)
- **Port**: 4200 (développement)
- **Service API**: `ApiService` pour communiquer avec le backend
- **Interface**: Dashboard moderne avec graphiques et tableaux interactifs

## 🚀 Démarrage Rapide

### 1. Prérequis
- Java 17+
- Node.js 16+
- Oracle Database
- Maven
- Angular CLI

### 2. Configuration Base de Données
```sql
-- Créer les tables (si nécessaire)
CREATE TABLE USERS (
    ID_USER NUMBER PRIMARY KEY,
    USERNAME VARCHAR2(50),
    EMAIL VARCHAR2(100),
    PASSWORD VARCHAR2(255),
    ROLE VARCHAR2(20),
    CREATED_AT DATE
);

CREATE TABLE FICHIERS (
    ID_FICHIER NUMBER PRIMARY KEY,
    ID_USER NUMBER,
    NOM_FICHIER VARCHAR2(255),
    TYPE_FICHIER VARCHAR2(50),
    NATURE_FICHIER VARCHAR2(50),
    CODE_VALEUR VARCHAR2(10),
    COD_EN VARCHAR2(10),
    SENS VARCHAR2(20),
    MONTANT NUMBER(10,2),
    NOMBER NUMBER,
    CREATED_AT DATE,
    UPDATED_AT DATE
);
```

### 3. Démarrage Backend
```bash
cd RUya
mvn spring-boot:run
```
Le serveur sera accessible sur `http://localhost:8081`

### 4. Démarrage Frontend
```bash
cd berry-free-angular-admin-template
ng serve
```
L'application sera accessible sur `http://localhost:4200`

## 🔗 Liaison Backend-Frontend

### Service API (`ApiService`)
```typescript
// berry-free-angular-admin-template/src/app/services/api.service.ts
export class ApiService {
  private baseUrl = 'http://localhost:8081/api';
  
  // Méthodes disponibles
  getStatsByStatus(): Observable<any>
  getMonthlyStats(): Observable<any>
  getAllFichiers(): Observable<FichierData[]>
  createFichier(fichier: any): Observable<any>
  updateFichier(id: number, fichier: any): Observable<any>
  deleteFichier(id: number): Observable<any>
  // ... autres méthodes
}
```

### Endpoints Backend
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/fichiers` | Tous les fichiers |
| GET | `/api/fichiers/stats/status` | Statistiques par statut |
| GET | `/api/fichiers/stats/monthly` | Statistiques mensuelles |
| GET | `/api/fichiers/alerts` | Alertes (fichiers rejetés) |
| POST | `/api/fichiers` | Créer un fichier |
| PUT | `/api/fichiers/{id}` | Modifier un fichier |
| DELETE | `/api/fichiers/{id}` | Supprimer un fichier |

## 📊 Fonctionnalités Liées

### 1. Dashboard Principal
- **Graphiques dynamiques**: Pie chart pour la distribution des statuts
- **Courbe mensuelle**: Évolution des fichiers et montants
- **Tableau interactif**: Filtres, tri, pagination
- **Alertes**: Fichiers rejetés récents

### 2. Gestion des Fichiers
- **Ajout de fichiers**: Modal avec validation
- **Modification**: Changement du nom uniquement
- **Suppression**: Avec confirmation
- **Affichage en cartes**: Design moderne et professionnel

### 3. Système d'Utilisateurs
- **Authentification**: Login, reset password
- **Rôles**: Admin/User avec permissions
- **Gestion des utilisateurs**: Interface dédiée

## 🎨 Interface Utilisateur

### Design System
- **Couleurs**: Orange/marron (thème banque)
- **Animations**: Transitions fluides
- **Responsive**: Adaptation mobile/desktop
- **Accessibilité**: Navigation clavier, contrastes

### Composants Principaux
- **Sidebar**: Navigation avec animations
- **Header**: Logo Attijari + titre RU'ya
- **Dashboard**: Graphiques ApexCharts
- **Modals**: Ajout/modification de fichiers

## 🔧 Configuration Technique

### Backend (Spring Boot)
```properties
# application.properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=ruya
spring.datasource.password=ruya123
server.port=8081
```

### Frontend (Angular)
```typescript
// Configuration CORS
@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOriginPatterns("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowCredentials(true);
  }
}
```

## 📈 Codes de Statut
| Code | Statut | Description |
|------|--------|-------------|
| 30 | REMIS | Fichier remis avec succès |
| 31 | REJET | Fichier rejeté |
| 32 | RENDU | Fichier rendu |
| 33 | EN_ATTENTE | Fichier en attente de traitement |

## 🛠️ Développement

### Structure des Données
```typescript
interface FichierData {
  id: number;
  nomFichier: string;
  typeFichier: string;
  natureFichier?: string;
  codeValeur?: string;
  codEn?: string;
  sens: string;
  montant: number;
  nomber: number;
  createdAt: string;
  user: {
    id: number;
    username: string;
    email: string;
  };
}
```

### Services Angular
- **ApiService**: Communication avec le backend
- **AjouterFichierService**: Gestion du modal d'ajout
- **AuthService**: Authentification
- **NotificationService**: Notifications utilisateur

## 🔍 Monitoring et Debug

### Logs Backend
```java
// FichierController.java
System.out.println("🔍 DEBUG - Requête POST reçue pour créer fichier: " + fichier);
System.err.println("❌ Erreur lors de la création du fichier: " + e.getMessage());
```

### Logs Frontend
```typescript
// Console browser
console.log('📊 Statistiques chargées:', data);
console.error('❌ Erreur lors du chargement des données:', error);
```

## 🚨 Dépannage

### Problèmes Courants
1. **Backend ne démarre pas**
   - Vérifier Oracle Database
   - Contrôler les credentials dans `application.properties`

2. **Erreurs CORS**
   - Vérifier la configuration `CorsConfig.java`
   - S'assurer que le backend tourne sur le port 8081

3. **Frontend ne se connecte pas**
   - Vérifier l'URL dans `ApiService`
   - Contrôler les logs du navigateur

4. **Données non affichées**
   - Vérifier la console browser
   - Contrôler les logs backend
   - Tester les endpoints avec Postman

### Commandes Utiles
```bash
# Vérifier le backend
curl -X GET http://localhost:8081/api/fichiers

# Vérifier les ports
netstat -an | findstr :8081

# Redémarrer les services
mvn spring-boot:run
ng serve
```

## 📝 Notes de Développement

### Fonctionnalités Implémentées
✅ Liaison backend-frontend complète  
✅ Dashboard avec graphiques dynamiques  
✅ Gestion CRUD des fichiers  
✅ Interface utilisateur moderne  
✅ Système d'authentification  
✅ Configuration CORS  
✅ Validation des données  
✅ Gestion d'erreurs robuste  

### Fonctionnalités Futures
🔄 Historique des utilisateurs  
🔄 Notifications en temps réel  
🔄 Export de données  
🔄 Rapports avancés  
🔄 API documentation (Swagger)  

## 👥 Équipe
- **Développement Backend**: Spring Boot, Java, Oracle
- **Développement Frontend**: Angular, TypeScript, SCSS
- **Design**: Interface moderne et professionnelle
- **Architecture**: Full-stack avec séparation claire des responsabilités

---

**RU'ya** - Application de gestion de fichiers financiers modernes et interactifs. 