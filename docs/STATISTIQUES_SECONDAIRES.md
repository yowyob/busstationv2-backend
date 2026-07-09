# Rapport d'Implémentation : Bloc Statistiques et Éléments Secondaires (Bloc 7)

Ce document décrit les endpoints pour les fonctionnalités de reporting, la découverte de trajets similaires, et la gestion des avantages financiers utilisateurs (coupons/indemnisations).

## 1. Statistiques Agence

### `GET /statistiques/agence/{id}/general`
Fournit les données agrégées pour le Dashboard Agence.

**Format de réponse (JSON) :**
```json
{
  "nombreEmployes": 15,
  "nombreChauffeurs": 10,
  "nombreVoyages": 150,
  "voyagesParStatut": {
    "PUBLIE": 120,
    "ANNULE": 5,
    "TERMINE": 25
  },
  "nombreReservations": 450,
  "reservationsParStatut": {
    "CONFIRMER": 400,
    "ANNULE": 50
  },
  "revenus": 2500000.0,
  "nouveauxUtilisateurs": 45,
  "tauxOccupation": 75.5
}
```

### `GET /statistiques/agence/{id}/evolution`
Fournit les données d'évolution temporelle pour les graphiques.

**Format de réponse (JSON) :**
```json
{
  "evolutionReservations": [
    { "date": "2026-04-01", "valeur": 10, "montant": 0.0 },
    { "date": "2026-04-02", "valeur": 15, "montant": 0.0 }
  ],
  "evolutionVoyages": [
    { "date": "2026-04-01", "valeur": 2, "montant": 0.0 }
  ],
  "evolutionRevenus": [
    { "date": "2026-04-01", "valeur": 0, "montant": 150000.0 }
  ],
  "evolutionUtilisateurs": [
    { "date": "2026-04-01", "valeur": 5, "montant": 0.0 }
  ]
}
```

### `GET /statistiques/agence/{id}/complete`
Combine les deux formats précédents dans un objet unique :
```json
{
  "general": { ... },
  "evolution": { ... }
}
```

---

## 2. Découverte de Voyages

### `GET /voyage/{id}/similaires`
Récupère une liste de voyages suggérés.

**Format de réponse (JSON - List<VoyagePreviewDTO>) :**
```json
[
  {
    "idVoyage": "uuid",
    "nomAgence": "General Express",
    "lieuDepart": "Yaoundé",
    "lieuArrive": "Douala",
    "nbrPlaceRestante": 15,
    "nbrPlaceReservable": 70,
    "dateDepartPrev": "2026-05-10T07:00:00",
    "dureeVoyage": "PT4H",
    "nomClasseVoyage": "Classique",
    "prix": 6000.0,
    "smallImage": "url",
    "bigImage": "url",
    "amenities": ["AC", "WIFI", "USB"],
    "statusVoyage": "PUBLIE"
  }
]
```

---

## 3. Avantages Financiers Utilisateurs

### Gestion des Coupons (`/coupon`)

#### `GET /coupon/user/{userId}`
**Format de réponse :**
```json
[
  {
    "idCoupon": "uuid",
    "dateDebut": "2026-04-15T10:00:00",
    "dateFin": "2026-07-15T10:00:00",
    "statusCoupon": "VALIDE",
    "valeur": 5000.0,
    "idHistorique": "uuid",
    "idSoldeIndemnisation": "uuid"
  }
]
```

#### `POST /coupon/apply`
Applique un coupon à une réservation.
**Paramètres Query :** `couponId`, `reservationId`, `userId`
**Réponse :** `true` (boolean) en cas de succès.

---

### Solde Indemnisation (`/solde-indemnisation`)

#### `GET /solde-indemnisation/user/{userId}`
Récupération paginée des soldes.
**Format de réponse :**
```json
[
  {
    "idSolde": "uuid",
    "solde": 15000.0,
    "type": "CASHBACK",
    "idUser": "uuid",
    "idAgenceVoyage": "uuid"
  }
]
```

---

## Notes Techniques et Améliorations Récentes

### Intégrité des Données et Enums
*   **Services Gare Routière** : L'énumération `ServicesGareRoutiere` a été étendue pour inclure tous les services standards (WIFI, CLIMATISATION, CONSIGNE, etc.). 
*   **Mapping Persistence** : Les mappers de persistence convertissent désormais correctement les listes de services en chaînes de caractères séparées par des virgules pour le stockage SQL.

### Optimisation des Requêtes
*   Correction des noms de colonnes SQL (`id_user` au lieu de `user_id`) dans le `SoldeIndemnisationR2dbcRepository`.
*   Suppression des tris sur des colonnes inexistantes (`updated_at`) pour éviter les erreurs 500.

### Workflow Développeur (DataSeeder)
*   Le `DataSeeder` est désormais **idempotent**. Il utilise la clause `ON CONFLICT (...) DO UPDATE` pour corriger les données existantes.
*   Il s'exécute à chaque démarrage pour garantir la synchronisation de l'environnement de développement.

---

## Notes pour le Frontend
*   **Durée** : Le champ `dureeVoyage` est au format ISO-8601 (ex: `PT4H` pour 4 heures).
*   **Coupons** : Ils sont générés automatiquement lors des annulations et peuvent être appliqués via `/coupon/apply` avant le paiement final d'une nouvelle réservation.
