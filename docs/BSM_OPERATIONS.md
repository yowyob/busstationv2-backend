# Rapport d'Implémentation : Bloc Bus Station Manager (Bloc 6)

Ce document décrit les endpoints dédiés au Bus Station Manager (BSM) pour la gestion opérationnelle des gares routières.

## 1. Statistiques Gare
### `GET /bsm/statistiques/{gareId}`
Récupère les KPIs de la gare pour le dashboard BSM.

*   **Réponse (BsmStatistiquesDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `gareId` | UUID | ID de la gare concernée |
    | `nbAgencesAffiliees` | Long | Nombre total d'agences rattachées |
    | `nbAgencesActives` | Long | Nombre d'agences actuellement actives |
    | `nbVoyagesAujourdhui`| Long | Nombre de voyages prévus aujourd'hui |
    | `nbVoyagesAVenir` | Long | Nombre de voyages publics à venir |
    | `tauxRemplissageMoyen`| Double | Taux de remplissage moyen des voyages |

---

## 2. Gestion Taxes d'Affiliation

### `POST /bsm/taxe-affiliation`
Création d'une nouvelle taxe d'affiliation pour les agences.
*   **Payload (TaxeAffiliationCreateDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `gareRoutiereId` | UUID | ID de la gare |
    | `nomTaxe` | String | Nom de la taxe |
    | `montantFixe` | Double | Montant de la taxe |

### `PUT /taxe-affiliation/{id}/statut`
Mise à jour du statut de paiement d'une taxe.

---

## 3. Politiques de Gare
### `POST /politique-gare`
Ajout de règles internes à la gare.
*   **Payload (PolitiqueGareCreateDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `gareRoutiereId` | UUID | ID de la gare |
    | `titre` | String | Titre de la règle |

---

## 4. Alertes
### `POST /alerte`
Envoi de notifications ou d'avertissements aux agences affiliées à la gare.
*   **Payload (AlerteCreateDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `agenceId` | UUID | ID de l'agence cible |
    | `message` | String | Contenu de l'alerte |
    | `typeAlerte` | Enum | Type (ex: TAX_REMINDER, ALERTE_GENERALE) |

---

## 5. Gestion des Agences (Administration BSM)
### `PUT /bsm/agence/{agenceId}/statut`
Suspension ou réactivation d'une agence par le BSM.
*   **Payload (UpdateStatutAgenceDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `active` | boolean | true pour activer, false pour suspendre |
    | `motif` | String | Raison de la décision |

---

## Notes pour le BSM
*   **Authentification** : Tous les endpoints BSM nécessitent le rôle `BUS_STATION_MANAGER`.
*   **Sécurité** : Le BSM ne peut effectuer des opérations (stats, alertes, gestion agences) que sur la gare dont il est le `managerId`.
*   **Données** : Les statistiques sont calculées en temps réel à partir de l'activité quotidienne de la gare.
