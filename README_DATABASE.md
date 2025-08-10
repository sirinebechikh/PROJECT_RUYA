# 🗄️ Guide de Liaison avec la Base de Données RU'ya

## 📋 Prérequis

### 1. Base de Données Oracle
- **Serveur**: Oracle Database (XEPDB1)
- **Port**: 1521
- **Utilisateur**: ruya
- **Mot de passe**: ruya123
- **Schéma**: ruya

### 2. Configuration Spring Boot
```properties
# application.properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=ruya
spring.datasource.password=ruya123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

## 🚀 Démarrage de l'Application

### 1. Démarrer le Backend Spring Boot
```bash
cd RUya
mvn spring-boot:run
```
- ✅ Serveur démarré sur `http://localhost:8081`
- ✅ APIs disponibles sur `/api/fichiers/*`

### 2. Démarrer le Frontend Angular
```bash
cd berry-free-angular-admin-template
ng serve
```
- ✅ Application accessible sur `http://localhost:4200`

## 🔗 Structure de Liaison

### 1. Modèle Fichier (Backend)
```java
@Entity
@Table(name = "FICHIERS")
public class Fichier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;
    
    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;
    
    @Column(name = "TYPE_FICHIER")
    private String typeFichier;
    
    @Column(name = "NATURE_FICHIER")
    private String natureFichier;
    
    @Column(name = "CODE_VALEUR")
    private String codeValeur;
    
    @Column(name = "MONTANT")
    private Double montant;
    
    @Column(name = "NOMBER")
    private Integer nomber;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
}
```

### 2. APIs REST Disponibles

#### 📊 Statistiques Dashboard
- `GET /api/fichiers/stats/status` - Statistiques par statut
- `GET /api/fichiers/stats/monthly` - Statistiques mensuelles
- `GET /api/fichiers/alerts` - Alertes (fichiers rejetés)

#### 📋 Gestion des Fichiers
- `GET /api/fichiers` - Liste tous les fichiers
- `POST /api/fichiers` - Créer un fichier
- `PUT /api/fichiers/{id}` - Modifier un fichier
- `DELETE /api/fichiers/{id}` - Supprimer un fichier

#### 🔍 Filtres et Recherche
- `GET /api/fichiers/filter` - Fichiers avec filtres avancés
- `GET /api/fichiers/pending` - Fichiers en attente
- `GET /api/fichiers/recent` - Fichiers récents

## 🎯 Fonctionnalités Liées

### 1. Dashboard en Temps Réel
- ✅ **Statistiques dynamiques** : REMIS, REJET, RENDU, EN_ATTENTE
- ✅ **Graphiques interactifs** : Pie chart et Line chart
- ✅ **Tableau filtré** : Recherche, tri, pagination
- ✅ **Alertes automatiques** : Fichiers rejetés récents

### 2. Gestion des Fichiers
- ✅ **Ajout de fichiers** : Validation complète côté client et serveur
- ✅ **Modification** : Préservation des extensions (.env, .rcp)
- ✅ **Suppression** : Avec confirmation
- ✅ **Affichage en cartes** : Design moderne et responsive

### 3. Système d'Utilisateurs
- ✅ **Authentification** : Gestion des rôles (ADMIN, USER)
- ✅ **Sécurité** : Validation des permissions
- ✅ **Session** : Stockage localStorage

## 🔧 Configuration de la Base de Données

### 1. Création des Tables
```sql
-- Table USERS
CREATE TABLE USERS (
    ID_USER NUMBER PRIMARY KEY,
    USERNAME VARCHAR2(50) NOT NULL,
    EMAIL VARCHAR2(100) UNIQUE,
    PASSWORD VARCHAR2(255) NOT NULL,
    ROLE VARCHAR2(20) DEFAULT 'USER',
    CREATED_AT TIMESTAMP DEFAULT SYSDATE
);

-- Table FICHIERS
CREATE TABLE FICHIERS (
    ID_FICHIER NUMBER PRIMARY KEY,
    ID_USER NUMBER NOT NULL,
    NOM_FICHIER VARCHAR2(255) NOT NULL,
    TYPE_FICHIER VARCHAR2(50),
    NATURE_FICHIER VARCHAR2(50),
    CODE_VALEUR VARCHAR2(10),
    COD_EN VARCHAR2(10),
    SENS VARCHAR2(20),
    MONTANT NUMBER(15,2),
    NOMBER NUMBER,
    CREATED_AT TIMESTAMP DEFAULT SYSDATE,
    UPDATED_AT TIMESTAMP DEFAULT SYSDATE,
    CONSTRAINT FK_FICHIERS_USER FOREIGN KEY (ID_USER) REFERENCES USERS(ID_USER)
);

-- Séquence pour l'auto-incrémentation
CREATE SEQUENCE SEQ_FICHIERS START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_USERS START WITH 1 INCREMENT BY 1;
```

### 2. Données de Test
```sql
-- Insertion d'utilisateurs de test
INSERT INTO USERS (ID_USER, USERNAME, EMAIL, PASSWORD, ROLE) 
VALUES (1, 'admin', 'admin@ruya.com', 'password', 'ADMIN');

INSERT INTO USERS (ID_USER, USERNAME, EMAIL, PASSWORD, ROLE) 
VALUES (2, 'user1', 'user1@ruya.com', 'password', 'USER');

-- Insertion de fichiers de test
INSERT INTO FICHIERS (ID_FICHIER, ID_USER, NOM_FICHIER, TYPE_FICHIER, CODE_VALEUR, SENS, MONTANT, NOMBER) 
VALUES (1, 1, 'Fichier_001.cheque', 'cheque', '30', 'emis', 5000.00, 10);
```

## 🛠️ Dépannage

### 1. Erreurs de Connexion Base de Données
```bash
# Vérifier la connexion Oracle
sqlplus ruya/ruya123@localhost:1521/XEPDB1

# Tester les APIs
curl http://localhost:8081/api/fichiers
```

### 2. Erreurs Frontend
```bash
# Nettoyer le cache Angular
ng cache clean

# Redémarrer le serveur
ng serve --poll=2000
```

### 3. Logs de Debug
```java
// Backend - Activer les logs SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

// Frontend - Console du navigateur
console.log('🔍 DEBUG - Données:', data);
```

## 📊 Monitoring

### 1. Métriques Dashboard
- **Fichiers par statut** : REMIS, REJET, RENDU, EN_ATTENTE
- **Évolution mensuelle** : Nombre de fichiers et montants
- **Alertes** : Fichiers rejetés dans les 30 derniers jours

### 2. Performance
- **Pagination** : 10 éléments par page
- **Filtres** : Date, statut, type, recherche textuelle
- **Tri** : Par nom, type, montant, date

## 🎉 Résultat Final

✅ **Backend Spring Boot** : APIs REST complètes avec gestion d'erreurs
✅ **Frontend Angular** : Dashboard moderne avec graphiques interactifs
✅ **Base de Données Oracle** : Structure optimisée avec contraintes
✅ **Sécurité** : Validation côté client et serveur
✅ **Performance** : Pagination et filtres avancés
✅ **Monitoring** : Statistiques en temps réel

L'application RU'ya est maintenant **entièrement fonctionnelle** avec tous les éléments liés à la base de données ! 🚀 