package cm.yowyob.bus_station_backend.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class DataSeeder {

    private final DatabaseClient db;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // UUIDs fixes (dérivés des IDs du db.json)
    // ─────────────────────────────────────────────

    // Gares
    private static final UUID GARE_1 = UUID.fromString("bb000002-0000-0000-0000-000000000001"); // Mvan Yaoundé
    private static final UUID GARE_2 = UUID.fromString("bb000002-0000-0000-0000-000000000002"); // Lac Municipal Yaoundé
    private static final UUID GARE_3 = UUID.fromString("bb000002-0000-0000-0000-000000000003"); // Bonabéri Douala
    private static final UUID GARE_4 = UUID.fromString("bb000002-0000-0000-0000-000000000004"); // Bessengué Douala
    private static final UUID GARE_5 = UUID.fromString("bb000002-0000-0000-0000-000000000005"); // Bafoussam

    // Organisations
    private static final UUID ORG_1 = UUID.fromString("cc000003-0000-0000-0000-000000000001");
    private static final UUID ORG_2 = UUID.fromString("cc000003-0000-0000-0000-000000000002");
    private static final UUID ORG_3 = UUID.fromString("cc000003-0000-0000-0000-000000000003");
    private static final UUID ORG_4 = UUID.fromString("cc000003-0000-0000-0000-000000000004");

    // Agences
    private static final UUID AGENCY_1 = UUID.fromString("dd000004-0000-0000-0000-000000000001");
    private static final UUID AGENCY_2 = UUID.fromString("dd000004-0000-0000-0000-000000000002");
    private static final UUID AGENCY_3 = UUID.fromString("dd000004-0000-0000-0000-000000000003");
    private static final UUID AGENCY_4 = UUID.fromString("dd000004-0000-0000-0000-000000000004");

    // Users BSM
    private static final UUID USER_BSM_1 = UUID.fromString("aa000001-0000-0000-0000-000000000001");
    private static final UUID USER_BSM_2 = UUID.fromString("aa000001-0000-0000-0000-000000000002");

    // Users responsables agences
    private static final UUID USER_RESP_1 = UUID.fromString("aa000001-0000-0000-0000-000000000011");
    private static final UUID USER_RESP_2 = UUID.fromString("aa000001-0000-0000-0000-000000000012");
    private static final UUID USER_RESP_3 = UUID.fromString("aa000001-0000-0000-0000-000000000013");
    private static final UUID USER_RESP_4 = UUID.fromString("aa000001-0000-0000-0000-000000000014");

    // Users clients
    private static final UUID USER_CLIENT_1 = UUID.fromString("aa000001-0000-0000-0000-000000000021");
    private static final UUID USER_CLIENT_2 = UUID.fromString("aa000001-0000-0000-0000-000000000022");
    private static final UUID USER_CLIENT_3 = UUID.fromString("aa000001-0000-0000-0000-000000000023");

    // Chauffeurs / Employés
    private static final UUID USER_CHAUFF_1  = UUID.fromString("aa000001-0000-0000-0000-000000000031"); // agency-001
    private static final UUID USER_CHAUFF_2  = UUID.fromString("aa000001-0000-0000-0000-000000000032"); // agency-001
    private static final UUID USER_CHAUFF_3  = UUID.fromString("aa000001-0000-0000-0000-000000000033"); // agency-001
    private static final UUID USER_EMP_4     = UUID.fromString("aa000001-0000-0000-0000-000000000034"); // agency-001 caissière
    private static final UUID USER_CHAUFF_4  = UUID.fromString("aa000001-0000-0000-0000-000000000035"); // agency-002
    private static final UUID USER_CHAUFF_5  = UUID.fromString("aa000001-0000-0000-0000-000000000036"); // agency-002
    private static final UUID USER_CHAUFF_7  = UUID.fromString("aa000001-0000-0000-0000-000000000037"); // agency-003
    private static final UUID USER_CHAUFF_8  = UUID.fromString("aa000001-0000-0000-0000-000000000038"); // agency-003
    private static final UUID USER_CHAUFF_10 = UUID.fromString("aa000001-0000-0000-0000-000000000040"); // agency-004

    // Véhicules
    private static final UUID VEH_1  = UUID.fromString("ee000005-0000-0000-0000-000000000001");
    private static final UUID VEH_2  = UUID.fromString("ee000005-0000-0000-0000-000000000002");
    private static final UUID VEH_3  = UUID.fromString("ee000005-0000-0000-0000-000000000003");
    private static final UUID VEH_4  = UUID.fromString("ee000005-0000-0000-0000-000000000004");
    private static final UUID VEH_5  = UUID.fromString("ee000005-0000-0000-0000-000000000005");
    private static final UUID VEH_6  = UUID.fromString("ee000005-0000-0000-0000-000000000006");
    private static final UUID VEH_7  = UUID.fromString("ee000005-0000-0000-0000-000000000007");
    private static final UUID VEH_8  = UUID.fromString("ee000005-0000-0000-0000-000000000008");
    private static final UUID VEH_9  = UUID.fromString("ee000005-0000-0000-0000-000000000009");
    private static final UUID VEH_10 = UUID.fromString("ee000005-0000-0000-0000-000000000010");
    private static final UUID VEH_11 = UUID.fromString("ee000005-0000-0000-0000-000000000011");
    private static final UUID VEH_13 = UUID.fromString("ee000005-0000-0000-0000-000000000013");
    private static final UUID VEH_14 = UUID.fromString("ee000005-0000-0000-0000-000000000014");

    // Classes voyage
    private static final UUID CL_1  = UUID.fromString("ff000007-0000-0000-0000-000000000001"); // GE Classique 6000
    private static final UUID CL_2  = UUID.fromString("ff000007-0000-0000-0000-000000000002"); // GE Confort 8500
    private static final UUID CL_3  = UUID.fromString("ff000007-0000-0000-0000-000000000003"); // GE Express 7500
    private static final UUID CL_4  = UUID.fromString("ff000007-0000-0000-0000-000000000004"); // GE Nuit Confort 9000
    private static final UUID CL_5  = UUID.fromString("ff000007-0000-0000-0000-000000000005"); // TE VIP Standard 15000
    private static final UUID CL_6  = UUID.fromString("ff000007-0000-0000-0000-000000000006"); // TE VIP Premium 22000
    private static final UUID CL_7  = UUID.fromString("ff000007-0000-0000-0000-000000000007"); // TE Business Class 35000
    private static final UUID CL_8  = UUID.fromString("ff000007-0000-0000-0000-000000000008"); // BTU Classique 5000
    private static final UUID CL_9  = UUID.fromString("ff000007-0000-0000-0000-000000000009"); // BTU Rapide 6500
    private static final UUID CL_10 = UUID.fromString("ff000007-0000-0000-0000-000000000010"); // CL Standard 4500
    private static final UUID CL_11 = UUID.fromString("ff000007-0000-0000-0000-000000000011"); // CL Confort 6000

    // Voyages
    private static final UUID VOY_1 = UUID.fromString("11000008-0000-0000-0000-000000000001");
    private static final UUID VOY_2 = UUID.fromString("11000008-0000-0000-0000-000000000002");
    private static final UUID VOY_3 = UUID.fromString("11000008-0000-0000-0000-000000000003");
    private static final UUID VOY_4 = UUID.fromString("11000008-0000-0000-0000-000000000004");
    private static final UUID VOY_5 = UUID.fromString("11000008-0000-0000-0000-000000000005");
    private static final UUID VOY_6 = UUID.fromString("11000008-0000-0000-0000-000000000006");
    private static final UUID VOY_7 = UUID.fromString("11000008-0000-0000-0000-000000000007");
    private static final UUID VOY_8 = UUID.fromString("11000008-0000-0000-0000-000000000008");

    // Lignes voyage
    private static final UUID LIGNE_1 = UUID.fromString("22000009-0000-0000-0000-000000000001");
    private static final UUID LIGNE_2 = UUID.fromString("22000009-0000-0000-0000-000000000002");
    private static final UUID LIGNE_3 = UUID.fromString("22000009-0000-0000-0000-000000000003");
    private static final UUID LIGNE_4 = UUID.fromString("22000009-0000-0000-0000-000000000004");
    private static final UUID LIGNE_5 = UUID.fromString("22000009-0000-0000-0000-000000000005");
    private static final UUID LIGNE_6 = UUID.fromString("22000009-0000-0000-0000-000000000006");
    private static final UUID LIGNE_7 = UUID.fromString("22000009-0000-0000-0000-000000000007");
    private static final UUID LIGNE_8 = UUID.fromString("22000009-0000-0000-0000-000000000008");

    // Employés
    private static final UUID EMP_1 = UUID.fromString("ee100001-0000-0000-0000-000000000004");

    // Chauffeurs
    private static final UUID CHAUFF_1 = UUID.fromString("cf000006-0000-0000-0000-000000000001");
    private static final UUID CHAUFF_2 = UUID.fromString("cf000006-0000-0000-0000-000000000002");
    private static final UUID CHAUFF_3 = UUID.fromString("cf000006-0000-0000-0000-000000000003");
    private static final UUID CHAUFF_4 = UUID.fromString("cf000006-0000-0000-0000-000000000004");
    private static final UUID CHAUFF_5 = UUID.fromString("cf000006-0000-0000-0000-000000000005");
    private static final UUID CHAUFF_7 = UUID.fromString("cf000006-0000-0000-0000-000000000006");
    private static final UUID CHAUFF_8 = UUID.fromString("cf000006-0000-0000-0000-000000000007");
    private static final UUID CHAUFF_10 = UUID.fromString("cf000006-0000-0000-0000-000000000008");

    // Affiliations
    private static final UUID AFF_1 = UUID.fromString("33000010-0000-0000-0000-000000000001"); // agency-001 → gare-1
    private static final UUID AFF_2 = UUID.fromString("33000010-0000-0000-0000-000000000002"); // agency-002 → gare-4
    private static final UUID AFF_3 = UUID.fromString("33000010-0000-0000-0000-000000000003"); // agency-003 → gare-5
    private static final UUID AFF_4 = UUID.fromString("33000010-0000-0000-0000-000000000004"); // agency-004 → gare-2
    private static final UUID AFF_5 = UUID.fromString("33000010-0000-0000-0000-000000000005"); // agency-001 → gare-4

    // Politique et taxes (gare)
    private static final UUID POL_1 = UUID.fromString("44000011-0000-0000-0000-000000000001");
    private static final UUID POL_2 = UUID.fromString("44000011-0000-0000-0000-000000000002");
    private static final UUID POL_3 = UUID.fromString("44000011-0000-0000-0000-000000000003");
    private static final UUID POL_4 = UUID.fromString("44000011-0000-0000-0000-000000000004");
    private static final UUID POL_5 = UUID.fromString("44000011-0000-0000-0000-000000000005");

    // Alertes
    private static final UUID ALERTE_1 = UUID.fromString("55000012-0000-0000-0000-000000000001");
    private static final UUID ALERTE_2 = UUID.fromString("55000012-0000-0000-0000-000000000002");
    private static final UUID ALERTE_3 = UUID.fromString("55000012-0000-0000-0000-000000000003");


    // ─────────────────────────────────────────────
    // Point d'entrée
    // ─────────────────────────────────────────────

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        log.info("🌱 Seeder — démarrage...");
        String pwd = passwordEncoder.encode("Password123");
        runAll(pwd)
                .doOnSuccess(v -> log.info("🎉 Seeder terminé avec succès !"))
                .doOnError(e -> log.error("❌ Erreur Seeder : {}", e.getMessage(), e))
                .subscribe();
    }

    private Mono<Void> runAll(String pwd) {
        return seedUsers(pwd)
                .then(seedOrganisations())
                .then(seedGares())
                .then(seedAgences())
                .then(seedVehicules())
                .then(seedChauffeurs())
                .then(seedEmployes())
                .then(seedClassVoyage())
                .then(seedVoyages())
                .then(seedLignesVoyage())
                .then(seedAffiliations())
                .then(seedPolitiquesGare())
                .then(seedAlertes());
    }

    // ─────────────────────────────────────────────
    // 1. GARES
    // ─────────────────────────────────────────────
    private Mono<Void> seedGares() {
        String sql = """
            INSERT INTO gare_routiere
              (id_gare_routiere, nom_gare_routiere, ville, quartier, description, horaires, services, manager_id, version)
            VALUES
              (:id, :nom, :ville, :quartier, :desc, :horaires, :services, :manager, 0)
            ON CONFLICT (id_gare_routiere) DO UPDATE SET services = EXCLUDED.services
            """;

        return db.sql(sql).bind("id", GARE_1).bind("nom", "Gare Routière de Mvan")
                .bind("ville", "Yaoundé").bind("quartier", "Mvan")
                .bind("desc", "Principal terminus Sud de Yaoundé, reliant la capitale aux villes du littoral, du Sud et du Centre.")
                .bind("horaires", "Lun–Dim : 04h00–23h00")
                .bind("services", "SALLE_ATTENTE,CLIMATISATION,CONSIGNE,RESTAURATION,WIFI,TOILETTES,PARKING,SECURITE")
                .bind("manager", USER_BSM_1).then()
                .then(db.sql(sql).bind("id", GARE_2).bind("nom", "Gare Routière du Lac Municipal")
                        .bind("ville", "Yaoundé").bind("quartier", "Centre-ville")
                        .bind("desc", "Gare centrale de Yaoundé, idéalement située en plein cœur de la ville.")
                        .bind("horaires", "Lun–Dim : 05h00–22h00")
                        .bind("services", "SALLE_ATTENTE,RESTAURATION,TOILETTES,PARKING,BILLETTERIE_ELECTRONIQUE")
                        .bind("manager", USER_BSM_2).then())
                .then(db.sql(sql).bind("id", GARE_3).bind("nom", "Gare Routière de Bonabéri")
                        .bind("ville", "Douala").bind("quartier", "Bonabéri")
                        .bind("desc", "Gare de Bonabéri, porte d'entrée Ouest de Douala.")
                        .bind("horaires", "Lun–Dim : 04h30–22h30")
                        .bind("services", "SALLE_ATTENTE,RESTAURATION,TOILETTES,PARKING,MOBILE_MONEY")
                        .bind("manager", USER_BSM_1).then())
                .then(db.sql(sql).bind("id", GARE_4).bind("nom", "Gare Routière de Bessengué")
                        .bind("ville", "Douala").bind("quartier", "Bessengué")
                        .bind("desc", "Gare centrale de Douala, au cœur du quartier Akwa.")
                        .bind("horaires", "Lun–Dim : 04h00–23h59")
                        .bind("services", "CLIMATISATION,WIFI,CONSIGNE,RESTAURATION,INFIRMERIE,PARKING,SECURITE")
                        .bind("manager", USER_BSM_2).then())
                .then(db.sql(sql).bind("id", GARE_5).bind("nom", "Gare Routière de Bafoussam")
                        .bind("ville", "Bafoussam").bind("quartier", "Banengo")
                        .bind("desc", "Principale gare de la capitale régionale de l'Ouest.")
                        .bind("horaires", "Lun–Dim : 05h00–21h00")
                        .bind("services", "SALLE_ATTENTE,RESTAURATION,TOILETTES,PARKING,BOUTIQUES")
                        .bind("manager", USER_BSM_1).then())
                .doOnSuccess(v -> log.info("  ✔ Gares insérées"));
    }

    // ─────────────────────────────────────────────
    // 2. ORGANISATIONS
    // ─────────────────────────────────────────────
    private Mono<Void> seedOrganisations() {
        String sql = "INSERT INTO organization (id, long_name, short_name, is_active) " +
                     "VALUES (:id, :longName, :shortName, true) ON CONFLICT (id) DO NOTHING";

        return db.sql(sql).bind("id", ORG_1).bind("longName", "General Express Cameroun SARL").bind("shortName", "GEC").then()
                .then(db.sql(sql).bind("id", ORG_2).bind("longName", "Touristique Express du Cameroun SA").bind("shortName", "TEC").then())
                .then(db.sql(sql).bind("id", ORG_3).bind("longName", "Bamiléké Transport Unité SARL").bind("shortName", "BTU").then())
                .then(db.sql(sql).bind("id", ORG_4).bind("longName", "Confort Lines Cameroun SARL").bind("shortName", "CLC").then())
                .doOnSuccess(v -> log.info("  ✔ Organisations insérées"));
    }

    // ─────────────────────────────────────────────
    // 3. UTILISATEURS
    // ─────────────────────────────────────────────
    private Mono<Void> seedUsers(String pwd) {
        String sql = """
            INSERT INTO users
              (user_id, username, prenom, nom, email, tel_number, password, roles, genre)
            VALUES
              (:id, :username, :first, :last, :email, :phone, :pwd, :roles, :gender)
            ON CONFLICT (user_id) DO UPDATE SET 
                username = EXCLUDED.username,
                email = EXCLUDED.email,
                tel_number = EXCLUDED.tel_number,
                password = EXCLUDED.password,
                roles = EXCLUDED.roles
            """;

        return
            // BSM
            db.sql(sql).bind("id", USER_BSM_1).bind("username", "bsm_mvan").bind("first", "Émile")
                .bind("last", "FOUDA NKOLO").bind("email", "e.fouda@gare-mvan.cm").bind("phone", "222305060")
                .bind("pwd", pwd).bind("roles", "BUS_STATION_MANAGER").bind("gender", "MALE").then()
            .then(db.sql(sql).bind("id", USER_BSM_2).bind("username", "bsm_lac").bind("first", "Cécile")
                .bind("last", "ABENA MANGA").bind("email", "c.abena@gare-lac.cm").bind("phone", "222312020")
                .bind("pwd", pwd).bind("roles", "BUS_STATION_MANAGER").bind("gender", "FEMALE").then())
            // Responsables agences
            .then(db.sql(sql).bind("id", USER_RESP_1).bind("username", "nkongo_theo").bind("first", "Théodore")
                .bind("last", "NKONGO").bind("email", "t.nkongo@generalexpress.cm").bind("phone", "655001122")
                .bind("pwd", pwd).bind("roles", "AGENCE_VOYAGE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_RESP_2).bind("username", "sandrine_te").bind("first", "Sandrine")
                .bind("last", "MOUKOURI").bind("email", "s.moukouri@touristiqueexpress.cm").bind("phone", "655778899")
                .bind("pwd", pwd).bind("roles", "AGENCE_VOYAGE").bind("gender", "FEMALE").then())
            .then(db.sql(sql).bind("id", USER_RESP_3).bind("username", "fotso_btu").bind("first", "Boniface")
                .bind("last", "FOTSO").bind("email", "b.fotso@btu-transport.cm").bind("phone", "656445566")
                .bind("pwd", pwd).bind("roles", "AGENCE_VOYAGE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_RESP_4).bind("username", "patricia_cl").bind("first", "Patricia")
                .bind("last", "OWONA").bind("email", "p.owona@confortlines.cm").bind("phone", "654223344")
                .bind("pwd", pwd).bind("roles", "AGENCE_VOYAGE").bind("gender", "FEMALE").then())
            // Clients
            .then(db.sql(sql).bind("id", USER_CLIENT_1).bind("username", "paul_mvondo").bind("first", "Paul")
                .bind("last", "MVONDO").bind("email", "paul.mvondo@gmail.com").bind("phone", "677112233")
                .bind("pwd", pwd).bind("roles", "USAGER").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CLIENT_2).bind("username", "mc_ngono").bind("first", "Marie-Claire")
                .bind("last", "NGONO").bind("email", "mc.ngono@yahoo.fr").bind("phone", "699887766")
                .bind("pwd", pwd).bind("roles", "USAGER").bind("gender", "FEMALE").then())
            .then(db.sql(sql).bind("id", USER_CLIENT_3).bind("username", "ibrahim_b").bind("first", "Ibrahim")
                .bind("last", "BELLO").bind("email", "ibrahim.bello@gmail.com").bind("phone", "676543210")
                .bind("pwd", pwd).bind("roles", "USAGER").bind("gender", "MALE").then())
            // Chauffeurs & Employés (rôle EMPLOYE)
            .then(db.sql(sql).bind("id", USER_CHAUFF_1).bind("username", "chauffeur_jp").bind("first", "Jean-Pierre")
                .bind("last", "MBARGA").bind("email", "jp.mbarga@generalexpress.cm").bind("phone", "655341122")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_2).bind("username", "chauffeur_herve").bind("first", "Hervé")
                .bind("last", "ESSOMBA").bind("email", "h.essomba@generalexpress.cm").bind("phone", "655560033")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_3).bind("username", "chauffeur_serge").bind("first", "Serge")
                .bind("last", "ATEBA").bind("email", "s.ateba@generalexpress.cm").bind("phone", "655789900")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_EMP_4).bind("username", "belibi_christelle").bind("first", "Christelle")
                .bind("last", "BELIBI").bind("email", "c.belibi@generalexpress.cm").bind("phone", "655110022")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "FEMALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_4).bind("username", "chauffeur_pierre").bind("first", "Pierre")
                .bind("last", "ABENA").bind("email", "p.abena@touristiqueexpress.cm").bind("phone", "655001199")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_5).bind("username", "chauffeur_paul").bind("first", "Paul")
                .bind("last", "NGUEMA").bind("email", "p.nguema@touristiqueexpress.cm").bind("phone", "655223300")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_7).bind("username", "chauffeur_aristide").bind("first", "Aristide")
                .bind("last", "KAMGA").bind("email", "a.kamga@btu-transport.cm").bind("phone", "656112233")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_8).bind("username", "chauffeur_roger").bind("first", "Roger")
                .bind("last", "TIENTCHEU").bind("email", "r.tientcheu@btu-transport.cm").bind("phone", "656445577")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .then(db.sql(sql).bind("id", USER_CHAUFF_10).bind("username", "chauffeur_eric").bind("first", "Eric")
                .bind("last", "ZAMBO").bind("email", "e.zambo@confortlines.cm").bind("phone", "654334455")
                .bind("pwd", pwd).bind("roles", "EMPLOYE").bind("gender", "MALE").then())
            .doOnSuccess(v -> log.info("  ✔ Utilisateurs insérés (20)"));
    }

    // ─────────────────────────────────────────────
    // 4. AGENCES
    // ─────────────────────────────────────────────
    private Mono<Void> seedAgences() {
        String sql = """
            INSERT INTO agences_voyage
              (agency_id, organisation_id, user_id, name, short_name, location, is_active,
               gare_routiere_id, moyens_paiement, vehicule_id_defaut, chauffeur_id_defaut, version)
            VALUES
              (:id, :orgId, :userId, :name, :shortName, :location, true,
               :gareId, CAST(:moyens AS jsonb), :vehId, :chauffId, 0)
            ON CONFLICT (agency_id) DO NOTHING
            """;

        return db.sql(sql)
                .bind("id", AGENCY_1).bind("orgId", ORG_1).bind("userId", USER_RESP_1)
                .bind("name", "General Express Yaoundé - Agence Principale").bind("shortName", "General Express")
                .bind("location", "Gare de Mvan, Yaoundé").bind("gareId", GARE_1)
                .bind("moyens", "[\"ORANGE_MONEY\",\"MTN_MOMO\",\"CASH\"]")
                .bind("vehId", VEH_1).bind("chauffId", USER_CHAUFF_1).then()
            .then(db.sql(sql)
                .bind("id", AGENCY_2).bind("orgId", ORG_2).bind("userId", USER_RESP_2)
                .bind("name", "Touristique Express VIP Douala").bind("shortName", "Touristique Express")
                .bind("location", "Gare de Bessengué, Douala").bind("gareId", GARE_4)
                .bind("moyens", "[\"ORANGE_MONEY\",\"MTN_MOMO\",\"EXPRESS_UNION\"]")
                .bind("vehId", VEH_5).bind("chauffId", USER_CHAUFF_4).then())
            .then(db.sql(sql)
                .bind("id", AGENCY_3).bind("orgId", ORG_3).bind("userId", USER_RESP_3)
                .bind("name", "BTU - Bafoussam Transit Unité").bind("shortName", "BTU Transport")
                .bind("location", "Gare de Banengo, Bafoussam").bind("gareId", GARE_5)
                .bind("moyens", "[\"ORANGE_MONEY\",\"MTN_MOMO\"]")
                .bind("vehId", VEH_9).bind("chauffId", USER_CHAUFF_7).then())
            .then(db.sql(sql)
                .bind("id", AGENCY_4).bind("orgId", ORG_4).bind("userId", USER_RESP_4)
                .bind("name", "Confort Lines - Axe Centre-Sud").bind("shortName", "Confort Lines")
                .bind("location", "Gare du Lac Municipal, Yaoundé").bind("gareId", GARE_2)
                .bind("moyens", "[\"ORANGE_MONEY\",\"CASH\"]")
                .bind("vehId", VEH_13).bind("chauffId", USER_CHAUFF_10).then())
            .doOnSuccess(v -> log.info("  ✔ Agences insérées (4)"));
    }

    // ─────────────────────────────────────────────
    // 5. VEHICULES
    // ─────────────────────────────────────────────
    private Mono<Void> seedVehicules() {
        String sql = """
            INSERT INTO vehicules
              (id_vehicule, nom, modele, description, nbr_places, plaque_matricule, lien_photo, id_agence_voyage)
            VALUES
              (:id, :nom, :modele, :desc, :places, :plaque, :photo, :agenceId)
            ON CONFLICT (id_vehicule) DO NOTHING
            """;

        return
            // Agency 1
            db.sql(sql).bind("id", VEH_1).bind("nom", "Grand Bus GE-01").bind("modele", "Yutong ZK6122H9")
                .bind("places", 70).bind("agenceId", AGENCY_1)
                .bind("desc", "Grand bus 70 places, climatisé, USB, écrans individuels.")
                .bind("plaque", "LT-7823-CM").bind("photo", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400").then()
            .then(db.sql(sql).bind("id", VEH_2).bind("nom", "Bus GE-02 Classique").bind("modele", "King Long XMQ6127J")
                .bind("places", 55).bind("agenceId", AGENCY_1)
                .bind("desc", "Bus 55 places, climatisé, idéal pour l'axe Yaoundé–Douala.")
                .bind("plaque", "LT-4456-CM").bind("photo", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=400").then())
            .then(db.sql(sql).bind("id", VEH_3).bind("nom", "Minibus GE-03 Express").bind("modele", "Toyota Hiace GL")
                .bind("places", 18).bind("agenceId", AGENCY_1)
                .bind("desc", "Minibus 18 places, express Yaoundé–Douala en 3h.")
                .bind("plaque", "LT-1102-CM").bind("photo", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400").then())
            .then(db.sql(sql).bind("id", VEH_4).bind("nom", "Bus GE-04 Nuit").bind("modele", "Higer KLQ6119Q")
                .bind("places", 45).bind("agenceId", AGENCY_1)
                .bind("desc", "Bus nuit 45 places, sièges inclinables 160°, couvertures fournies.")
                .bind("plaque", "LT-9908-CM").bind("photo", "https://images.unsplash.com/photo-1494515843206-f3117d3f51b7?w=400").then())
            // Agency 2
            .then(db.sql(sql).bind("id", VEH_5).bind("nom", "VIP Coach TE-01").bind("modele", "Mercedes-Benz Travego")
                .bind("places", 30).bind("agenceId", AGENCY_2)
                .bind("desc", "Coach grand tourisme 30 places VIP, sièges cuir, Wi-Fi, toilettes à bord.")
                .bind("plaque", "LT-0055-CM").bind("photo", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400").then())
            .then(db.sql(sql).bind("id", VEH_6).bind("nom", "VIP Premium TE-02").bind("modele", "Volvo 9700")
                .bind("places", 35).bind("agenceId", AGENCY_2)
                .bind("desc", "Bus premium 35 places, double deck, panoramique, climatisation individuelle.")
                .bind("plaque", "LT-2277-CM").bind("photo", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400").then())
            .then(db.sql(sql).bind("id", VEH_7).bind("nom", "Minibus TE-03 Affaires").bind("modele", "Mercedes-Benz Sprinter 519")
                .bind("places", 16).bind("agenceId", AGENCY_2)
                .bind("desc", "Minibus affaires 16 places, sièges cuir, tablettes individuelles, Wi-Fi.")
                .bind("plaque", "LT-6643-CM").bind("photo", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400").then())
            .then(db.sql(sql).bind("id", VEH_8).bind("nom", "Touristic TE-04 Tour").bind("modele", "Irizar i8")
                .bind("places", 40).bind("agenceId", AGENCY_2)
                .bind("desc", "Coach de luxe 40 places, vitres panoramiques, guide touristique optionnel.")
                .bind("plaque", "LT-3381-CM").bind("photo", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400").then())
            // Agency 3
            .then(db.sql(sql).bind("id", VEH_9).bind("nom", "BTU Grand Bus-01").bind("modele", "Yutong ZK6112HG")
                .bind("places", 50).bind("agenceId", AGENCY_3)
                .bind("desc", "Bus 50 places, climatisé, pour liaisons Bafoussam–Douala et Bafoussam–Yaoundé.")
                .bind("plaque", "OU-1134-CM").bind("photo", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400").then())
            .then(db.sql(sql).bind("id", VEH_10).bind("nom", "BTU Express-02").bind("modele", "King Long XMQ6100")
                .bind("places", 40).bind("agenceId", AGENCY_3)
                .bind("desc", "Bus 40 places, rapidité et fiabilité sur l'axe Ouest.")
                .bind("plaque", "OU-5567-CM").bind("photo", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400").then())
            .then(db.sql(sql).bind("id", VEH_11).bind("nom", "BTU Minibus-03").bind("modele", "Toyota Coaster HZB50")
                .bind("places", 30).bind("agenceId", AGENCY_3)
                .bind("desc", "Minibus 30 places, desserte des villes secondaires de l'Ouest.")
                .bind("plaque", "OU-8890-CM").bind("photo", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400").then())
            // Agency 4
            .then(db.sql(sql).bind("id", VEH_13).bind("nom", "CL Minibus-01 Sud").bind("modele", "Toyota HiAce Commuter")
                .bind("places", 18).bind("agenceId", AGENCY_4)
                .bind("desc", "Minibus 18 places, axe Yaoundé–Mbalmayo–Ebolowa.")
                .bind("plaque", "CE-2201-CM").bind("photo", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=400").then())
            .then(db.sql(sql).bind("id", VEH_14).bind("nom", "CL Minibus-02 Kribi").bind("modele", "Nissan Urvan NV350")
                .bind("places", 15).bind("agenceId", AGENCY_4)
                .bind("desc", "Minibus 15 places, liaison directe Yaoundé–Kribi.")
                .bind("plaque", "CE-4423-CM").bind("photo", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=400").then())
            .doOnSuccess(v -> log.info("  ✔ Véhicules insérés (13)"));
    }

    // ─────────────────────────────────────────────
    // 6. CLASSES VOYAGE
    // ─────────────────────────────────────────────
    private Mono<Void> seedClassVoyage() {
        String sql = "INSERT INTO class_voyage (id, label, price, is_active, version, id_agence_voyage) " +
                     "VALUES (:id, :label, :price, true, 0, :agenceId) ON CONFLICT (id) DO NOTHING";

        return
            db.sql(sql).bind("id", CL_1).bind("label", "Classique GE").bind("price", 6000.0).bind("agenceId", AGENCY_1).then()
            .then(db.sql(sql).bind("id", CL_2).bind("label", "Confort GE").bind("price", 8500.0).bind("agenceId", AGENCY_1).then())
            .then(db.sql(sql).bind("id", CL_3).bind("label", "Express GE").bind("price", 7500.0).bind("agenceId", AGENCY_1).then())
            .then(db.sql(sql).bind("id", CL_4).bind("label", "Nuit Confort GE").bind("price", 9000.0).bind("agenceId", AGENCY_1).then())
            .then(db.sql(sql).bind("id", CL_5).bind("label", "VIP Standard TE").bind("price", 15000.0).bind("agenceId", AGENCY_2).then())
            .then(db.sql(sql).bind("id", CL_6).bind("label", "VIP Premium TE").bind("price", 22000.0).bind("agenceId", AGENCY_2).then())
            .then(db.sql(sql).bind("id", CL_7).bind("label", "Business Class TE").bind("price", 35000.0).bind("agenceId", AGENCY_2).then())
            .then(db.sql(sql).bind("id", CL_8).bind("label", "Classique BTU").bind("price", 5000.0).bind("agenceId", AGENCY_3).then())
            .then(db.sql(sql).bind("id", CL_9).bind("label", "Rapide BTU").bind("price", 6500.0).bind("agenceId", AGENCY_3).then())
            .then(db.sql(sql).bind("id", CL_10).bind("label", "Standard CL").bind("price", 4500.0).bind("agenceId", AGENCY_4).then())
            .then(db.sql(sql).bind("id", CL_11).bind("label", "Confort CL").bind("price", 6000.0).bind("agenceId", AGENCY_4).then())
            .doOnSuccess(v -> log.info("  ✔ Classes voyage insérées (11)"));
    }

    // ─────────────────────────────────────────────
    // 7. EMPLOYES
    // ─────────────────────────────────────────────
    private Mono<Void> seedEmployes() {
        String sqlEmp = """
            INSERT INTO employes
              (id, user_id, agence_id, poste, date_embauche, departement, statut_employe)
            VALUES
              (:id, :userId, :agenceId, :poste, :dateEmb, :dept, :statut)
            ON CONFLICT (id) DO NOTHING
            """;

        return
            db.sql(sqlEmp)
                .bind("id", EMP_1).bind("userId", USER_EMP_4).bind("agenceId", AGENCY_1)
                .bind("poste", "Caissière").bind("dept", "Finance")
                .bind("dateEmb", OffsetDateTime.parse("2020-01-06T00:00:00Z")).bind("statut", "ACTIF")
                .then()
            .doOnSuccess(v -> log.info("  ✔ Employés insérés (1)"));
    }

    private Mono<Void> seedChauffeurs() {
        String sqlChauff = """
            INSERT INTO chauffeurs
              (id, user_id, agence_id, statut, annee_experience, numero_permis)
            VALUES
              (:id, :userId, :agenceId, :statut, :experience, :permis)
            ON CONFLICT (id) DO NOTHING
            """;

        return
            db.sql(sqlChauff)
                .bind("id", CHAUFF_1).bind("userId", USER_CHAUFF_1).bind("agenceId", AGENCY_1)
                .bind("statut", "LIBRE").bind("experience", 10).bind("permis", "CNI-CM-1234567").then()
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_2).bind("userId", USER_CHAUFF_2).bind("agenceId", AGENCY_1)
                .bind("statut", "LIBRE").bind("experience", 5).bind("permis", "CNI-CM-2345678").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_3).bind("userId", USER_CHAUFF_3).bind("agenceId", AGENCY_1)
                .bind("statut", "LIBRE").bind("experience", 8).bind("permis", "CNI-CM-3456789").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_4).bind("userId", USER_CHAUFF_4).bind("agenceId", AGENCY_2)
                .bind("statut", "LIBRE").bind("experience", 15).bind("permis", "CNI-CM-5678901").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_5).bind("userId", USER_CHAUFF_5).bind("agenceId", AGENCY_2)
                .bind("statut", "LIBRE").bind("experience", 7).bind("permis", "CNI-CM-6789012").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_7).bind("userId", USER_CHAUFF_7).bind("agenceId", AGENCY_3)
                .bind("statut", "LIBRE").bind("experience", 12).bind("permis", "CNI-CM-7890123").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_8).bind("userId", USER_CHAUFF_8).bind("agenceId", AGENCY_3)
                .bind("statut", "LIBRE").bind("experience", 9).bind("permis", "CNI-CM-8901234").then())
            .then(db.sql(sqlChauff)
                .bind("id", CHAUFF_10).bind("userId", USER_CHAUFF_10).bind("agenceId", AGENCY_4)
                .bind("statut", "LIBRE").bind("experience", 6).bind("permis", "CNI-CM-9012345").then())
            .doOnSuccess(v -> log.info("  ✔ Chauffeurs insérés (8)"));
    }

    // ─────────────────────────────────────────────
    // 8. VOYAGES
    // ─────────────────────────────────────────────
    private Mono<Void> seedVoyages() {
        String sql = """
            INSERT INTO voyages
              (id_voyage, titre, description, lieu_depart, lieu_arrive, point_de_depart, point_arrivee,
               status_voyage, date_publication, date_depart_prev, date_limite_reservation, date_limite_confirmation,
               nbr_place_reservable, nbr_place_restante, nbr_place_reserve, nbr_place_confirm,
               duree_voyage, amenities, small_image, big_image)
            VALUES
              (:id, :titre, :desc, :dep, :arr, :ptDep, :ptArr,
               :statut, :pubDate, :depDate, :limResa, :limConf,
               :reservable, :restante, :reserve, :confirm,
               :duree, :amenities, :smallImg, :bigImg)
            ON CONFLICT (id_voyage) DO NOTHING
            """;

        return
            // voy-001 GE Classique
            db.sql(sql).bind("id", VOY_1).bind("titre", "Yaoundé → Douala — Classique Express")
                .bind("desc", "Départ de Mvan à 07h00. Voyage direct, climatisé. Arrivée Gare de Bessengué.")
                .bind("dep", "Yaoundé").bind("arr", "Douala").bind("ptDep", "Gare Routière de Mvan").bind("ptArr", "Gare de Bessengué")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-25T08:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-10T07:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-09T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-09T23:59:00Z"))
                .bind("reservable", 53).bind("restante", 23).bind("reserve", 30).bind("confirm", 28)
                .bind("duree", 14400L).bind("amenities", "AC,USB,COMFORTABLE_SEATS,LUGGAGE_STORAGE")
                .bind("smallImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200").then()
            // voy-002 GE Confort Midi
            .then(db.sql(sql).bind("id", VOY_2).bind("titre", "Yaoundé → Douala — Confort Midi")
                .bind("desc", "Départ de Mvan à 12h00. Voyage express sans arrêt. Arrivée Bessengué.")
                .bind("dep", "Yaoundé").bind("arr", "Douala").bind("ptDep", "Gare Routière de Mvan").bind("ptArr", "Gare de Bessengué")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-25T08:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-10T12:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-09T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-09T23:59:00Z"))
                .bind("reservable", 53).bind("restante", 23).bind("reserve", 30).bind("confirm", 28)
                .bind("duree", 13500L).bind("amenities", "AC,USB,COMFORTABLE_SEATS,WIFI")
                .bind("smallImg", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=1200").then())
            // voy-003 TE VIP Standard
            .then(db.sql(sql).bind("id", VOY_3).bind("titre", "Yaoundé → Douala — VIP Standard")
                .bind("desc", "Service VIP direct Yaoundé–Douala. Sièges cuir, Wi-Fi haut débit, collation offerte.")
                .bind("dep", "Yaoundé").bind("arr", "Douala").bind("ptDep", "Gare du Lac Municipal").bind("ptArr", "Gare de Bessengué")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-24T10:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-10T08:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-09T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-09T23:59:00Z"))
                .bind("reservable", 28).bind("restante", 6).bind("reserve", 22).bind("confirm", 20)
                .bind("duree", 12600L).bind("amenities", "AC,WIFI,USB,COMFORTABLE_SEATS,MEAL_SERVICE,ENTERTAINMENT,POWER_OUTLETS")
                .bind("smallImg", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=1200").then())
            // voy-004 GE Nuit Confort
            .then(db.sql(sql).bind("id", VOY_4).bind("titre", "Yaoundé → Douala — Nuit Confort")
                .bind("desc", "Voyage de nuit climatisé. Couvertures et oreiller fournis.")
                .bind("dep", "Yaoundé").bind("arr", "Douala").bind("ptDep", "Gare Routière de Mvan").bind("ptArr", "Gare de Bonabéri")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-25T08:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-10T22:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-10T18:00:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-10T18:00:00Z"))
                .bind("reservable", 44).bind("restante", 6).bind("reserve", 38).bind("confirm", 35)
                .bind("duree", 16200L).bind("amenities", "AC,COMFORTABLE_SEATS,LUGGAGE_STORAGE")
                .bind("smallImg", "https://images.unsplash.com/photo-1494515843206-f3117d3f51b7?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1494515843206-f3117d3f51b7?w=1200").then())
            // voy-005 BTU Bafoussam–Douala
            .then(db.sql(sql).bind("id", VOY_5).bind("titre", "Bafoussam → Douala — Classique")
                .bind("desc", "Liaison directe Bafoussam–Douala via Bafang. Départ de la Gare de Banengo.")
                .bind("dep", "Bafoussam").bind("arr", "Douala").bind("ptDep", "Gare de Banengo").bind("ptArr", "Gare de Bonabéri")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-26T07:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-11T06:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-10T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-10T23:59:00Z"))
                .bind("reservable", 48).bind("restante", 23).bind("reserve", 25).bind("confirm", 20)
                .bind("duree", 18000L).bind("amenities", "AC,COMFORTABLE_SEATS,LUGGAGE_STORAGE")
                .bind("smallImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200").then())
            // voy-006 GE Classique Matin 12 mai
            .then(db.sql(sql).bind("id", VOY_6).bind("titre", "Yaoundé → Douala — Classique Matin (12 mai)")
                .bind("desc", "Départ matinal de la Gare de Mvan. Arrêt à Edéa. Service classique.")
                .bind("dep", "Yaoundé").bind("arr", "Douala").bind("ptDep", "Gare Routière de Mvan").bind("ptArr", "Gare de Bonabéri")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-27T08:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-12T06:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-11T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-11T23:59:00Z"))
                .bind("reservable", 68).bind("restante", 56).bind("reserve", 12).bind("confirm", 10)
                .bind("duree", 15300L).bind("amenities", "AC,USB,COMFORTABLE_SEATS")
                .bind("smallImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200").then())
            // voy-007 TE VIP Premium Douala–Yaoundé
            .then(db.sql(sql).bind("id", VOY_7).bind("titre", "Douala → Yaoundé — VIP Premium")
                .bind("desc", "Retour VIP Douala–Yaoundé en fin de journée. Plateau dînatoire inclus. Bus panoramique.")
                .bind("dep", "Douala").bind("arr", "Yaoundé").bind("ptDep", "Gare de Bessengué").bind("ptArr", "Gare du Lac Municipal")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-26T09:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-11T17:00:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-11T12:00:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-11T12:00:00Z"))
                .bind("reservable", 33).bind("restante", 13).bind("reserve", 20).bind("confirm", 18)
                .bind("duree", 12600L).bind("amenities", "AC,WIFI,USB,MEAL_SERVICE,ENTERTAINMENT,POWER_OUTLETS,COMFORTABLE_SEATS")
                .bind("smallImg", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1557223562-6c77ef16210f?w=1200").then())
            // voy-008 CL Yaoundé–Mbalmayo
            .then(db.sql(sql).bind("id", VOY_8).bind("titre", "Yaoundé → Mbalmayo — Standard Matin")
                .bind("desc", "Liaison rapide Yaoundé–Mbalmayo. Départ de la Gare du Lac Municipal.")
                .bind("dep", "Yaoundé").bind("arr", "Mbalmayo").bind("ptDep", "Gare du Lac Municipal").bind("ptArr", "Gare de Mbalmayo")
                .bind("statut", "PUBLIE").bind("pubDate", OffsetDateTime.parse("2026-04-25T08:00:00Z")).bind("depDate", OffsetDateTime.parse("2026-05-10T07:30:00Z"))
                .bind("limResa", OffsetDateTime.parse("2026-05-09T23:59:00Z")).bind("limConf", OffsetDateTime.parse("2026-05-09T23:59:00Z"))
                .bind("reservable", 16).bind("restante", 6).bind("reserve", 10).bind("confirm", 9)
                .bind("duree", 5400L).bind("amenities", "AC,COMFORTABLE_SEATS")
                .bind("smallImg", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=400")
                .bind("bigImg", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=1200").then())
            .doOnSuccess(v -> log.info("  ✔ Voyages insérés (8)"));
    }

    // ─────────────────────────────────────────────
    // 9. LIGNES VOYAGE
    // ─────────────────────────────────────────────
    private Mono<Void> seedLignesVoyage() {
        String sql = """
            INSERT INTO lignes_voyage
              (id_ligne_voyage, id_voyage, id_agence_voyage, id_vehicule, id_chauffeur, id_class_voyage)
            VALUES
              (:id, :voyId, :agenceId, :vehId, :chauffId, :classId)
            ON CONFLICT (id_ligne_voyage) DO NOTHING
            """;

        return
            db.sql(sql).bind("id", LIGNE_1).bind("voyId", VOY_1).bind("agenceId", AGENCY_1)
                .bind("vehId", VEH_1).bind("chauffId", CHAUFF_1).bind("classId", CL_1).then()
            .then(db.sql(sql).bind("id", LIGNE_2).bind("voyId", VOY_2).bind("agenceId", AGENCY_1)
                .bind("vehId", VEH_2).bind("chauffId", CHAUFF_2).bind("classId", CL_2).then())
            .then(db.sql(sql).bind("id", LIGNE_3).bind("voyId", VOY_3).bind("agenceId", AGENCY_2)
                .bind("vehId", VEH_5).bind("chauffId", CHAUFF_4).bind("classId", CL_5).then())
            .then(db.sql(sql).bind("id", LIGNE_4).bind("voyId", VOY_4).bind("agenceId", AGENCY_1)
                .bind("vehId", VEH_4).bind("chauffId", CHAUFF_3).bind("classId", CL_4).then())
            .then(db.sql(sql).bind("id", LIGNE_5).bind("voyId", VOY_5).bind("agenceId", AGENCY_3)
                .bind("vehId", VEH_9).bind("chauffId", CHAUFF_7).bind("classId", CL_8).then())
            .then(db.sql(sql).bind("id", LIGNE_6).bind("voyId", VOY_6).bind("agenceId", AGENCY_1)
                .bind("vehId", VEH_1).bind("chauffId", CHAUFF_1).bind("classId", CL_1).then())
            .then(db.sql(sql).bind("id", LIGNE_7).bind("voyId", VOY_7).bind("agenceId", AGENCY_2)
                .bind("vehId", VEH_6).bind("chauffId", CHAUFF_5).bind("classId", CL_6).then())
            .then(db.sql(sql).bind("id", LIGNE_8).bind("voyId", VOY_8).bind("agenceId", AGENCY_4)
                .bind("vehId", VEH_13).bind("chauffId", CHAUFF_10).bind("classId", CL_10).then())
            .doOnSuccess(v -> log.info("  ✔ Lignes voyage insérées (8)"));
    }

    // ─────────────────────────────────────────────
    // 10. AFFILIATIONS
    // ─────────────────────────────────────────────
    private Mono<Void> seedAffiliations() {
        String sql = """
            INSERT INTO affiliation_agence_voyage
              (id, gare_routiere_id, agency_id, agency_name, statut, echeance, montant_affiliation)
            VALUES
              (:id, :gareId, :agenceId, :agenceName, :statut, :echeance, :montant)
            ON CONFLICT (id) DO UPDATE SET statut = EXCLUDED.statut
            """;

        return
            db.sql(sql).bind("id", AFF_1).bind("gareId", GARE_1).bind("agenceId", AGENCY_1)
                .bind("agenceName", "General Express Yaoundé").bind("statut", "PAYE")
                .bind("echeance", LocalDate.parse("2026-05-01")).bind("montant", 75000.0).then()
            .then(db.sql(sql).bind("id", AFF_2).bind("gareId", GARE_4).bind("agenceId", AGENCY_2)
                .bind("agenceName", "Touristique Express VIP Douala").bind("statut", "PAYE")
                .bind("echeance", LocalDate.parse("2026-05-01")).bind("montant", 90000.0).then())
            .then(db.sql(sql).bind("id", AFF_3).bind("gareId", GARE_5).bind("agenceId", AGENCY_3)
                .bind("agenceName", "BTU - Bafoussam Transit Unité").bind("statut", "PAYE")
                .bind("echeance", LocalDate.parse("2026-05-01")).bind("montant", 55000.0).then())
            .then(db.sql(sql).bind("id", AFF_4).bind("gareId", GARE_2).bind("agenceId", AGENCY_4)
                .bind("agenceName", "Confort Lines - Axe Centre-Sud").bind("statut", "PAYE")
                .bind("echeance", LocalDate.parse("2026-05-01")).bind("montant", 65000.0).then())
            .then(db.sql(sql).bind("id", AFF_5).bind("gareId", GARE_4).bind("agenceId", AGENCY_1)
                .bind("agenceName", "General Express Yaoundé").bind("statut", "PAYE")
                .bind("echeance", LocalDate.parse("2026-05-01")).bind("montant", 80000.0).then())
            .doOnSuccess(v -> log.info("  ✔ Affiliations insérées (5)"));
    }

    // ─────────────────────────────────────────────
    // 11. POLITIQUES ET TAXES (Gares)
    // ─────────────────────────────────────────────
    private Mono<Void> seedPolitiquesGare() {
        String sql = """
            INSERT INTO politique_et_taxes
              (id_politique, gare_routiere_id, nom_politique, description, montant_fixe, date_effet, type)
            VALUES
              (:id, :gareId, :nom, :desc, :montant, :dateEffet, :type)
            ON CONFLICT (id_politique) DO NOTHING
            """;

        return
            db.sql(sql).bind("id", POL_1).bind("gareId", GARE_1).bind("nom", "Règlement intérieur — Gare de Mvan")
                .bind("desc", "Toute agence affiliée s'engage à respecter les horaires de départ affichés.")
                .bindNull("montant", Double.class).bind("dateEffet", LocalDate.parse("2024-01-01")).bind("type", "POLITIQUE").then()
            .then(db.sql(sql).bind("id", POL_2).bind("gareId", GARE_1).bind("nom", "Taxe d'affiliation mensuelle — Mvan")
                .bind("desc", "Chaque agence affiliée à la Gare de Mvan s'acquitte d'une taxe mensuelle d'affiliation fixée à 75 000 FCFA.")
                .bind("montant", 75000.0).bind("dateEffet", LocalDate.parse("2024-01-01")).bind("type", "TAXE").then())
            .then(db.sql(sql).bind("id", POL_3).bind("gareId", GARE_1).bind("nom", "Frais de quai par départ — Mvan")
                .bind("desc", "Un frais de quai de 500 FCFA est prélevé par départ effectif.")
                .bind("montant", 500.0).bind("dateEffet", LocalDate.parse("2024-01-01")).bind("type", "TAXE").then())
            .then(db.sql(sql).bind("id", POL_4).bind("gareId", GARE_2).bind("nom", "Politique de sécurité — Gare du Lac")
                .bind("desc", "Identification obligatoire des passagers à l'embarquement.")
                .bindNull("montant", Double.class).bind("dateEffet", LocalDate.parse("2023-06-01")).bind("type", "POLITIQUE").then())
            .then(db.sql(sql).bind("id", POL_5).bind("gareId", GARE_2).bind("nom", "Taxe d'affiliation — Gare du Lac")
                .bind("desc", "Taxe mensuelle d'affiliation fixée à 65 000 FCFA pour toute agence occupant un guichet à la Gare du Lac.")
                .bind("montant", 65000.0).bind("dateEffet", LocalDate.parse("2023-06-01")).bind("type", "TAXE").then())
            .doOnSuccess(v -> log.info("  ✔ Politiques & taxes insérées (5)"));
    }

    // ─────────────────────────────────────────────
    // 12. ALERTES AGENCE
    // ─────────────────────────────────────────────
    private Mono<Void> seedAlertes() {
        String sql = """
            INSERT INTO alertes_agence
              (id_alerte, gare_id, agence_id, bsm_id, type, message, is_lu, created_at)
            VALUES
              (:id, :gareId, :agenceId, :bsmId, :type, :message, :isLu, :createdAt)
            ON CONFLICT (id_alerte) DO NOTHING
            """;

        return
            db.sql(sql).bind("id", ALERTE_1).bind("gareId", GARE_2).bind("agenceId", AGENCY_4)
                .bind("bsmId", USER_BSM_2).bind("type", "TAX_REMINDER")
                .bind("message", "Rappel : votre taxe d'affiliation du mois de mars 2026 (65 000 FCFA) est en retard. Merci de régulariser avant le 10 mai 2026.")
                .bind("isLu", true).bind("createdAt", OffsetDateTime.parse("2026-04-15T10:00:00Z")).then()
            .then(db.sql(sql).bind("id", ALERTE_2).bind("gareId", GARE_2).bind("agenceId", AGENCY_4)
                .bind("bsmId", USER_BSM_2).bind("type", "ALERTE_GENERALE")
                .bind("message", "Votre agence a été signalée pour un départ non déclaré le 22 avril 2026 à 14h30. Un deuxième manquement entraînera une suspension temporaire.")
                .bind("isLu", false).bind("createdAt", OffsetDateTime.parse("2026-04-23T09:00:00Z")).then())
            .then(db.sql(sql).bind("id", ALERTE_3).bind("gareId", GARE_1).bind("agenceId", AGENCY_1)
                .bind("bsmId", USER_BSM_1).bind("type", "ALERTE_GENERALE")
                .bind("message", "Information : la Gare de Mvan procédera à des travaux de rénovation du 5 au 10 juin 2026. Les départs seront temporairement relocalisés vers la plateforme Est.")
                .bind("isLu", true).bind("createdAt", OffsetDateTime.parse("2026-04-20T11:00:00Z")).then())
            .doOnSuccess(v -> log.info("  ✔ Alertes insérées (3)"));
    }
}