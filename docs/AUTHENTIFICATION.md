# Rapport d'Implémentation : Bloc Authentification (Bloc 1)

Ce document décrit les endpoints nécessaires à l'authentification et à la gestion du profil utilisateur. Tous les endpoints nécessitant une authentification doivent inclure le token dans le header : `Authorization: Bearer <accessToken>`.

## 1. Inscription
### `POST /auth/register`
Inscrit un nouvel utilisateur (client ou professionnel).

*   **Payload (UserDTO)** :
    ```json
    {
      "email": "test@yowyob.cm",
      "username": "testuser",
      "password": "Password123!",
      "last_name": "Test",
      "first_name": "User",
      "phone_number": "677123456",
      "gender": "MALE",
      "role": ["USAGER"] 
    }
    ```
*   **Réponse (UserResponseCreatedDTO)** : Retourne les informations de base de l'utilisateur créé (`userId`, `email`, etc.) avec un statut `201 Created`.

## 2. Connexion
### `POST /auth/login`
Authentifie l'utilisateur et retourne les jetons de session.

*   **Payload (AuthentificationDTO)** :
    ```json
    {
      "username": "testuser",
      "password": "Password123!"
    }
    ```
*   **Réponse (AuthTokensDTO)** :
    ```json
    {
      "accessToken": "eyJ...",
      "refreshToken": "eyJ...",
      "expiresIn": 3600,
      "user": { ... }
    }
    ```
*   **Notes importantes** : Le `accessToken` doit être stocké en mémoire sécurisée et utilisé dans le header `Authorization` pour tous les appels suivants.

## 3. Profil Utilisateur
### `GET /auth/me`
Récupère les informations détaillées de l'utilisateur connecté.

*   **Réponse (UserResponseDTO)** : Objet complet contenant les détails de l'utilisateur (nom, prénom, email, rôles, etc.).

### `PUT /auth/me`
Met à jour le profil de l'utilisateur connecté.

*   **Payload (UserDTO)** : Identique à l'inscription, met à jour uniquement les champs fournis.

## 4. Récupération de compte
### `POST /auth/forgot-password`
Demande une réinitialisation de mot de passe par email.

*   **Payload** : `{"email": "test@yowyob.cm"}`
*   **Réponse** : Toujours un message générique pour éviter l'énumération des emails.

### `POST /auth/reset-password`
Finalise le changement de mot de passe avec le jeton reçu.

*   **Payload (ResetPasswordRequestDTO)** :
    ```json
    {
      "token": "token-recu-par-email",
      "newPassword": "NewPassword123!"
    }
    ```

---

## Éléments à garder pour les prochains workflows

Pour les prochaines fonctionnalités (Réservation, Voyage, etc.), assurez-vous de conserver ces éléments :

1.  **Gestion des Tokens (Auth)** :
    *   Le `accessToken` doit être systématiquement envoyé dans l'en-tête : `Authorization: Bearer <votre_token>`.
    *   La gestion de l'expiration du token doit être implémentée : si une requête retourne `401 Unauthorized`, utiliser le `refreshToken` pour appeler `POST /auth/refresh` afin d'obtenir un nouveau token sans forcer la reconnexion de l'utilisateur.

2.  **Rôles utilisateurs** :
    *   Lors de l'appel `/auth/me`, inspectez le champ `role` (ex: `["USAGER"]`, `["BUS_STATION_MANAGER"]`). Certaines fonctionnalités (ex: création de voyage) ne seront disponibles que si l'utilisateur possède le rôle requis.

3.  **Gestion des erreurs** :
    *   `400 Bad Request` : Généralement dû à une validation échouée (format email, mot de passe trop court).
    *   `401 Unauthorized` : Token invalide ou expiré (nécessite une reconnexion ou un refresh).
    *   `409 Conflict` : Conflit lors de l'inscription (ex: email ou téléphone déjà utilisé).

4.  **Types de données** :
    *   Toutes les dates côté serveur sont désormais normalisées en `LocalDateTime` (ISO-8601). Veillez à envoyer vos dates dans ce format.
