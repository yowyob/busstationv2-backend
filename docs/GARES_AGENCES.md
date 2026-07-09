# Rapport d'Implémentation : Bloc Gares Routières et Agences (Bloc 2)

Ce document décrit les endpoints pour la gestion des gares routières et des agences de voyage.

## 1. Gares Routières
### POST /gare
Crée une nouvelle gare routière.
*   **Payload (GareRoutiereRequestDTO)** :
    ```json
    {
      "nomGareRoutiere": "Gare de Yaoundé",
      "ville": "Yaoundé",
      "quartier": "Mvan",
      "nomPresident": "Président Test",
      "managerId": "uuid-du-manager"
    }
    ```

### GET /gare
Liste les gares routières (paginé).

### GET /gare/{id}
Détails d'une gare routière.

### PUT /gare/{id}
Met à jour une gare routière.

## 2. Agences de Voyage
### POST /agence
Crée une nouvelle agence.
*   **Payload (AgenceVoyageDTO)** :
    ```json
    {
      "long_name": "Agence Nom Complet",
      "short_name": "ANC",
      "location": "Yaoundé",
      "organisation_id": "uuid-org",
      "user_id": "uuid-chef-agence",
      "gare_routiere_id": "uuid-gare"
    }
    ```

### GET /agence/{id}
Détails d'une agence.

### PATCH /agence/{id}
Mise à jour partielle (ex: `gare_routiere_id` peut être mis à jour).

## 3. Affiliation
### POST /affiliation
Lie une agence à une gare routière (utilisé si l'agence doit être rattachée à plusieurs gares ou pour les cas de multi-affiliation).
*   **Payload (AffiliationCreateDTO)** : 
    ```json
    {
      "gareRoutiereId": "...",
      "agencyId": "...",
      "montantAffiliation": 10000.0,
      "echeance": "2026-06-01"
    }
    ```

---

## Notes pour le Frontend
*   **Création Agence** : Le champ `gare_routiere_id` est **obligatoire**. L'agence ne peut plus être créée sans être immédiatement rattachée à une gare.
*   **Affiliation** : Bien que l'agence soit rattachée à une gare à la création, l'endpoint `/affiliation` reste disponible pour gérer les cas d'agences multi-sites ou multi-gares.
*   **Vue Publique** : Utilisez `/agence/{id}/public` pour les vues destinées aux usagers finaux, cette vue est optimisée pour la lecture seule.
*   **Paiement** : La configuration des moyens de paiement se fait via `/agence/{id}/moyens-paiement` et est essentielle pour le workflow de réservation.
