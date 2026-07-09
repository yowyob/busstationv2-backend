# Rapport d'Implémentation : Bloc Réservation et Paiement (Bloc 5)

Ce document décrit le workflow complet de réservation, du choix du voyage au paiement final, ainsi que les procédures d'annulation.

## 1. Création de la Réservation

### `POST /reservation/reserver`
Initialise une réservation pour un voyage spécifique. Les places sont bloquées (statut `RESERVER`) en attendant le paiement.

*   **Payload (ReservationDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `idVoyage` | UUID | ID du voyage sélectionné |
    | `nbrPassager` | int | Nombre total de passagers |
    | `passagerDTO` | List | Liste des informations passagers (voir ci-dessous) |

*   **Détail Passager (PassagerDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `nom` | String | Nom complet |
    | `genre` | Enum | `MALE` ou `FEMALE` |
    | `age` | int | Âge du passager |
    | `nbrBaggage` | int | Nombre de bagages |
    | `placeChoisis`| int | Numéro de place sélectionné (optionnel) |

*   **Réponse (Reservation)** : Retourne l'objet réservation créé avec son `idReservation` et son statut initial.

---

## 2. Tunnel de Paiement

### `POST /paiement/initier`
Déclenche une demande de paiement auprès de l'agrégateur (ex: demande de PIN sur le téléphone).

*   **Payload (PayRequestDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `reservationId` | UUID | ID de la réservation concernée |
    | `amount` | double | Montant à payer |
    | `mobilePhone` | String | Numéro de téléphone (ex: "677...") |
    | `mobilePhoneName`| String | Nom de l'opérateur (ex: "MTN", "ORANGE") |

*   **Réponse (PayInResultDTO)** : Contient le `transactionCode` généré par le système.

### `POST /paiement/confirmer` (Webhook simulation)
Simule le retour de l'opérateur de paiement pour confirmer le succès de la transaction.

*   **Payload (PaiementCallbackDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `transactionCode`| String | Code reçu lors de l'initiation |
    | `reservationId` | UUID | ID de la réservation |
    | `montantPaye` | double | Montant réellement débité |

*   **Réponse (ReservationDetailDTO)** : Retourne la réservation mise à jour (Statut: `CONFIRMER`, Paiement: `PAID`).

---

## 3. Gestion des Réservations Utilisateur

### `GET /reservation/user/{userId}`
Récupère l'historique des réservations d'un utilisateur (paginé).

*   **Réponse** : `Page<ReservationPreviewDTO>`
    Chaque élément contient :
    *   `reservation` : Détails financiers et statut.
    *   `voyage` : Infos sur le trajet (date, départ, arrivée).
    *   `agence` : Nom et logo de l'agence.

---

## 4. Annulation

### `POST /reservation/annuler/{reservationId}`
Permet à un utilisateur d'annuler tout ou partie de sa réservation.

*   **Payload (ReservationCancelDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `idPassagers` | List<UUID> | IDs des passagers à annuler |
    | `causeAnnulation`| String | Motif de l'annulation |
    | `canceled` | boolean | `true` pour confirmer l'action |

*   **Conséquences** :
    *   Les places sont libérées dans l'inventaire du voyage.
    *   Un coupon de remboursement est généré selon la politique d'annulation de l'agence.

### `POST /reservation/agence/annuler-voyage`
Action administrative déclenchée par l'agence (ou un admin) pour annuler un voyage complet.

*   **Payload (VoyageCancelDTO)** :
    | Champ | Type | Description |
    | :--- | :--- | :--- |
    | `idVoyage` | UUID | ID du voyage à annuler |
    | `agenceVoyageId` | UUID | ID de l'agence |
    | `canceled` | boolean | `true` pour confirmer |

*   **Conséquences** : Toutes les réservations liées passent au statut `ANNULER` et les clients sont notifiés.

---

## Notes pour le Frontend

### États de Réservation
1.  **RESERVER** : En attente de paiement. Les places sont bloquées temporairement.
2.  **CONFIRMER** : Paiement validé. Le billet est généré et prêt pour l'embarquement.
3.  **ANNULER** : Réservation annulée par l'utilisateur ou l'agence.

### Gestion du Temps de Blocage
Les réservations au statut `RESERVER` ont une date limite (généralement 2h avant le départ). Passé ce délai, un scheduler automatique les annule pour libérer les places.

### Remboursements
Le système ne gère pas de "Cashback" direct sur compte bancaire. Les annulations génèrent des **Coupons** (Stockés dans le profil utilisateur) réutilisables pour de futures réservations auprès de la même agence.
