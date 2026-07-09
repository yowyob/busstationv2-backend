#!/bin/bash
# ============================================================
# verify_seed.sh — Vérification post-seeder Bus Station
# Usage : bash verify_seed.sh
# ============================================================

echo "🔍 Vérification du seeder..."

CMD="docker compose exec -T postgres psql -U root -d bus_station_db -t -c"

check() {
  local label=$1
  local sql=$2
  local expected=$3
  local count
  count=$($CMD "$sql" | tr -d ' \n')
  if [ "$count" -ge "$expected" ]; then
    echo "  ✅ $label : $count (attendu ≥ $expected)"
  else
    echo "  ❌ $label : $count (attendu ≥ $expected)"
  fi
}

check "Gares"         "SELECT COUNT(*) FROM gare_routiere;"               5
check "Organisations" "SELECT COUNT(*) FROM organization;"                4
check "Users"         "SELECT COUNT(*) FROM users;"                       20
check "Agences"       "SELECT COUNT(*) FROM agences_voyage;"              4
check "Véhicules"     "SELECT COUNT(*) FROM vehicules;"                   13
check "Classes"       "SELECT COUNT(*) FROM class_voyage;"                11
check "Employés"      "SELECT COUNT(*) FROM employes;"                    9
check "Voyages"       "SELECT COUNT(*) FROM voyages;"                     8
check "Lignes"        "SELECT COUNT(*) FROM lignes_voyage;"               8
check "Affiliations"  "SELECT COUNT(*) FROM affiliation_agence_voyage;"   5
check "Politiques"    "SELECT COUNT(*) FROM politique_et_taxes;"          5
check "Alertes"       "SELECT COUNT(*) FROM alertes_agence;"              3

echo ""
echo "🔐 Comptes de test disponibles (mot de passe : Password123) :"
echo "  BSM       : bsm_mvan / bsm_lac"
echo "  Agence    : nkongo_theo / sandrine_te / fotso_btu / patricia_cl"
echo "  Clients   : paul_mvondo / mc_ngono / ibrahim_b"
echo "  Chauffeurs: chauffeur_jp / chauffeur_herve / chauffeur_serge"
echo "              chauffeur_pierre / chauffeur_paul / chauffeur_aristide"
echo "              chauffeur_roger / chauffeur_eric"