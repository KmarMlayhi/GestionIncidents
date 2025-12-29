#  Application de Gestion des Incidents Municipaux

## Description générale

Cette application web permet aux **citoyens** de signaler des incidents urbains (éclairage, propreté, sécurité, infrastructure, etc.), aux **administrateurs** de gérer et répartir ces incidents par département, et aux **agents municipaux** de traiter les incidents qui leur sont assignés.

L’objectif principal est d’améliorer la communication entre citoyens et municipalité et d’assurer un traitement rapide, structuré et traçable des incidents.

## 👩‍💻 Réalisé par

**Kmar Malyhi** -
**Oumaima Mzoughi**

Dans le cadre du module : * Développement Web Avancé (DWA) *  

Année universitaire : 2025 – 2026

---

# Rôles et fonctionnalités

##  Super Administrateur

* Création des comptes administrateurs et agents
* Affectation des agents à leurs administrateurs
* Consultation Dashboard

## Administrateur

* Accès aux incidents correspondant à son **département**
* Visualisation des détails complets d’un incident (description, photos, citoyen)
* Choisir de la priorité de l’incident
* Affectation d’un incident à un agent de son département
* Suivi de l’état des incidents
* Consultation Dashboard
* Consultation de son profil


## Citoyen

* Création de compte avec vérification par email
* Déclaration d’un incident avec :
  * Titre et description
  * Catégorie (Infrastructure, Propreté, Sécurité, Éclairage)
  * Quartier
  * Upload de photos
  * Géolocalisation (latitude / longitude)
* Consultation de ses incidents
* Consultation Dashboard
* Consultation de son profil

## Agent municipal

* Consultation des incidents qui lui sont assignés
* Visualisation des informations et photos de l’incident
* Mise à jour de l’état de traitement (en cours de résolution --> résolu)
* Consultation Dashboard
* Consultation de son profil
---

# Architecture technique

## Backend

* **Spring Boot**
* **Spring Security** (authentification et autorisation)
* **Spring Data JPA / Hibernate**
* Base de données relationnelle (**MySQL**)

## Frontend

* **Thymeleaf**
* JavaScript
* Leaflet (carte statique pour la géolocalisation)
---

# Gestion des images
* Stockage local dans le dossier :

/uploads/incidents

---

# Sécurité

* Authentification basée sur Spring Security (JDBC)
* Gestion des rôles : `CITOYEN`, `AGENT`, `ADMIN`, `SUPER_ADMIN`
* Accès restreint aux fonctionnalités selon le rôle
* Déconnexion via endpoint `/logout`

---

# Lancement du projet

1. Cloner le projet
2. Configurer la base de données dans les variables d'environnment (run -> edit configurations -> modify option -> environment variable)
3. Créer la base MySQL : Création du base de données "Gestion incidents" dans mysql
5. Lancer l’application

6. Accéder à la base de données :
   
```
http://localhost/phpmyadmin/
```

6. Accéder à l’application :

```
http://localhost:8080/login
```
## DevOps 
# Configuration (Variables d’environnement)
Les secrets (DB, mail...) **ne doivent pas être commit** --> Doivent figurés dans le fichier .env et ce fichier dans .gitignore
