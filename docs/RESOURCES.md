# Rapport d'Implémentation : Bloc Gestion des Ressources (Bloc 3)

Ce document décrit les endpoints pour la gestion des ressources opérationnelles d'une agence de voyage : véhicules, chauffeurs et classes de voyage.

## 1. Véhicules

### POST /vehicule
Ajoute un nouveau véhicule à la flotte.
*   **Payload (VehiculeDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `nom` | String | Nom du véhicule |
    | `modele` | String | Modèle du véhicule |
    | `description` | String | Description optionnelle |
    | `nbrPlaces` | int | Nombre de places disponibles |
    | `plaqueMatricule` | String | Plaque d'immatriculation unique |
    | `lienPhoto` | String | URL de la photo |
    | `idAgenceVoyage` | UUID | ID de l'agence propriétaire |

### GET /vehicule/agence/{agenceId}
Liste les véhicules d'une agence.
*   **Réponse** : `List<VehiculeDTO>`

---

## 2. Chauffeurs

### POST /utilisateur/chauffeur
Ajoute un nouveau chauffeur.
*   **Payload (ChauffeurRequestDTO)** : Hérite de `UserDTO` + champs spécifiques.
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `last_name` | String | Nom de famille |
    | `first_name` | String | Prénom |
    | `email` | String | Email unique |
    | `username` | String | Nom d'utilisateur |
    | `password` | String | Mot de passe |
    | `phone_number` | String | Numéro de téléphone |
    | `gender` | Enum | MALE, FEMALE |
    | `role` | List<RoleType> | Doit inclure `CHAUFFEUR` |
    | `agenceVoyageId` | UUID | Agence liée |

### GET /chauffeur/agence/{agenceId}
Liste les chauffeurs affiliés à une agence.
*   **Réponse** : `List<UserResponseDTO>`

---

## 3. Classes de Voyage

### POST /class-voyage
Crée une nouvelle classe (ex: VIP, Standard).
*   **Payload (ClassVoyageDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `nom` | String | Nom de la classe |
    | `prix` | double | Prix de base |
    | `idAgenceVoyage` | UUID | Agence liée |

### GET /class-voyage/agence/{agenceId}
Liste les classes d'une agence.
*   **Réponse** : `List<ClassVoyageDTO>`

---

## Notes pour le Frontend
*   **Dépendances** : La création d'un voyage nécessite la sélection d'un véhicule et d'une classe de voyage.
*   **Rôles** : L'ajout d'un chauffeur requiert le rôle `CHAUFFEUR` dans la liste des rôles envoyée.
*   **Validation** : Le système applique des validations strictes sur les données (ex: email valide, prix positif). Les erreurs seront renvoyées avec un code `400 Bad Request` et un message explicatif.
