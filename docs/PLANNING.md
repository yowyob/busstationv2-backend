# Rapport d'Implémentation : Bloc Planning et Génération (Bloc 4)

Ce document décrit le cœur opérationnel du système : la gestion du planning récurrent et la génération automatique des voyages publiables ou des brouillons en cas de ressources manquantes.

## 1. Gestion des Lignes de Service (Planning)

### POST `/ligne-service`
Définit un planning récurrent pour une agence.
*   **Payload (PlanningVoyageDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `idAgenceVoyage` | UUID | ID de l'agence propriétaire |
    | `nom` | String | Nom du planning (ex: "Saison Été 2026") |
    | `recurrence` | Enum | `QUOTIDIEN`, `HEBDOMADAIRE`, `MENSUEL` |
    | `dateDebut` | LocalDate | Date de début d'application (YYYY-MM-DD) |
    | `dateFin` | LocalDate | Date de fin optionnelle |
    | `creneaux` | List | Liste des créneaux (voir ci-dessous) |

*   **Détail d'un Créneau (CreneauPlanningDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `jourSemaine` | Enum | `MONDAY`, `TUESDAY`, etc. |
    | `heureDepart` | LocalTime | Heure au format `HH:mm:ss` |
    | `heureArrivee` | LocalTime | Heure d'arrivée estimée |
    | `lieuDepart` | String | Ville de départ |
    | `lieuArrive` | String | Ville d'arrivée |
    | `idClassVoyage`| UUID | Classe tarifaire par défaut |
    | `idVehicule` | UUID | Véhicule affecté (optionnel) |
    | `idChauffeur` | UUID | Chauffeur affecté (optionnel) |
    | `nbrPlacesDisponibles` | int | Capacité du bus |

### GET `/ligne-service/agence/{agenceId}`
Récupère tous les plannings d'une agence.
*   **Réponse** : `List<PlanningVoyageDTO>`

---

## 2. Génération de Voyages

### POST `/voyage/generer-unitaire`
Génère un voyage réel à partir d'un créneau de planning pour une date précise.
*   **Payload (GenerationUnitaireRequestDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `ligneServiceId` | UUID | ID du créneau (creneauId) |
    | `dateDepartPrev` | LocalDate | Date réelle du voyage |
    | `publierDirectement` | boolean | Si true, tente de publier immédiatement |

*   **Réponse (GenerationResultDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `statut` | String | `PUBLIE` (succès) ou `INCOMPLET` (brouillon créé) |
    | `voyageId` | UUID | Présent si statut est `PUBLIE` |
    | `brouillonId` | UUID | Présent si statut est `INCOMPLET` |
    | `conflits` | List<String>| Raisons du statut `INCOMPLET` (ex: "CHAUFFEUR_MANQUANT") |
    | `message` | String | Message informatif |

---

## 3. Gestion des Brouillons (Correction)

### GET `/voyage/brouillon/agence/{id}`
Récupère les brouillons à compléter pour une agence.
*   **Réponse** : `List<VoyageBrouillonResponseDTO>`

### PUT `/voyage/brouillon/{id}`
Permet de modifier un brouillon pour résoudre les conflits (ex: ajouter le chauffeur manquant).
*   **Payload (VoyageBrouillonUpdateDTO)** : Tous les champs du voyage sont optionnels. Seuls les champs non-null seront mis à jour.
*   **Réponse** : `VoyageBrouillonResponseDTO` contenant le nouveau `statutBrouillon` (`INCOMPLET` ou `PRET`).

### POST `/voyage/brouillon/{id}/publier`
Finalise le voyage.
*   **Condition** : Le brouillon doit être au statut `PRET` (toutes les infos requises sont présentes).
*   **Réponse (VoyageDetailsDTO)** : Le voyage officiel créé.

---

## Notes pour le Frontend

### États du Brouillon
1.  **INCOMPLET** : Il manque des informations critiques (Chauffeur, Véhicule, Prix, Dates limites). Le bouton "Publier" doit être désactivé.
2.  **PRET** : Toutes les ressources sont assignées. L'utilisateur peut cliquer sur "Publier".

### Algorithme de Matching (Côté Serveur)
Lors de la génération, le serveur vérifie la disponibilité :
*   Le véhicule ne doit pas être déjà assigné à un autre voyage `PUBLIE` sur la même plage horaire.
*   Le chauffeur ne doit pas être en voyage ou en repos.
*   Si le matching échoue, le système crée un brouillon au lieu d'un voyage pour permettre une résolution manuelle.

### Formats de Données
*   **Dates** : `2026-05-02` (LocalDate)
*   **Heures** : `08:00:00` (LocalTime)
*   **DateHeure** : `2026-05-02T08:00:00` (LocalDateTime)
