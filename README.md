#  Application de Gestion des Incidents Municipaux

## Description générale
Cette application web permet aux **citoyens** de signaler des incidents urbains (éclairage, propreté, sécurité, infrastructure, etc.), aux **administrateurs** de gérer et répartir ces incidents par département, et aux **agents municipaux** de traiter les incidents qui leur sont assignés.
L’objectif principal est d’améliorer la communication entre citoyens et municipalité et d’assurer un traitement rapide, structuré et traçable des incidents.
## Réalisé par
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
## DevOps Travail réalisé par :
équipe de Dev: ** Kmar Mlayhi ** et ** Oumaima Mzoughi ** 
équipe de Test: ** Ons Fitouri ** et ** Siwar Labassi **

## Répartition du travail :

Kmar : Dockerisation

Oumaima : Intégration Continue (CI)

Ons : Tests d’intégration

Siwar : Tests unitaires

Toute l’équipe : CD et déploiement sur Azure

## Présentation du Projet
Le projet utilise Docker pour l’exécution en conteneur et un pipeline CI/CD basé sur GitHub Actions pour l’intégration continue et le déploiement automatique. L’infrastructure de déploiement repose sur Microsoft Azure DevOps, utilisé pour la création de la machine virtuelle et le déploiement de l’application en environnement de production.
## Réalisation
1. Prérequis
Java 17
Maven
Docker & Docker Compose (pour exécution locale ou déploiement)
GitHub Actions pour CI/CD
Azure Devops

2. Instructions pour les développeurs et les testeurs
# Compiler et packager l’application
mvn clean package

# Lancer l’application
java -jar target/app.jar

# Réalisation des Tests unitaires
Les tests unitaires vérifient le bon fonctionnement de la logique métier de manière isolée.
Ils utilisent JUnit 5 et Mockito pour simuler les dépendances.
IncidentWorkflowServiceTest :
Validation du workflow d’assignation d’un incident (assignation de l’agent, changement d’état, envoi des emails et sauvegarde).
AgentIncidentServiceTest :
Vérification de la récupération des incidents assignés à l’agent connecté avec contrôle du rôle AGENT.
AccountServiceTest :
Validation de la création des comptes utilisateurs (génération et encodage du mot de passe, intégration Spring Security, persistance et gestion des erreurs).

# Réalisation de Test d'intégration 
Ce test vérifie le workflow d’assignation d’un incident dans un contexte Spring réel, avec JPA/Hibernate et H2 en mémoire, en validant le démarrage du contexte, la logique métier et la persistance des données.

# Lancer tous les tests unitaires et d’intégration
mvn test

# Lancer les conteneurs Docker (local/dev)
1- Démarrer les conteneurs app + base de données MySQL
docker-compose up --build

2- Déploiement en production (VM)

* Pull de l'image Docker et lancement sur la VM
docker compose -f docker-compose.deploy.yml pull app
docker compose -f docker-compose.deploy.yml up -d --no-deps app

-> Le script deploy.sh automatise :
la mise à jour du code sur la VM
le pull de la nouvelle image Docker
le lancement du conteneur app en vérifiant que db est prêt

## pipeline CI/CD
CI (Continuous Integration)
Déclenchement : à chaque push sur dev ou main, ou pull request sur dev
Étapes principales :
Checkout du code depuis GitHub
Configuration de Java 17
Compilation et packaging du projet Maven → génération du JAR (app-jar)
Exécution des tests automatisés
Upload du JAR comme artifact (stockage temporaire)
CD (Continuous Deployment)
Déclenchement : à chaque push sur main

### Étapes principales :
Build & test Maven pour sécurité
Construction de l’image Docker via le Dockerfile (contenant le JAR)
Push de l’image sur le GitHub Container Registry (GHCR)
Déploiement sur la VM via deploy.sh + docker-compose.deploy.yml
Pull de la nouvelle image
Lancement du conteneur app
Vérification de la santé du service db avant démarrage

lien pour application hébergée : http://4.178.105.15:8080/login


