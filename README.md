# Bus Station

Ce projet est une plateforme multi-tenante de gestion complète destinée aux gares routières et aux agences de transport.
Il vise à moderniser, automatiser et centraliser toutes les opérations liées au fonctionnement d’une gare, depuis
l’administration des agences jusqu’à la gestion du trafic des véhicules.
Il s’intègre dans un écosystème plus large comprenant une application mobile et d'autres services backend.
Le système permet d’enregistrer et de gérer les sociétés de transport, leurs agences, ainsi que leurs véhicules. Il
assure le contrôle et le suivi des opérations quotidiennes d’une gare : validation des sociétés, organisation des
départs et arrivées, gestion du ticket de quai, collecte des taxes, gestion des sanctions et supervision du
stationnement et ainsi que la gestion des reservations de voyage pour les clients.


## Architecture du projet

Ce projet est conçu selon les principes de [l'Architecture Hexagonale](https://fr.wikipedia.org/wiki/Architecture_hexagonale).


## Technologies utilisées

- [Java 17+ / JDK17+](https://www.oracle.com/java/technologies/downloads/#jdk25-windows)
- [Postgresql](https://www.postgresql.org/download/)
- [Spring Boot(Maven)](https://maven.apache.org/download.cgi) (Ou en passant par [Chocolatey](https://chocolatey.org/install#individual))
- Redis (L'installation dépend de l'OS, sur Windows vous pouvez utiliser WSL et installer redis)


## SetUp

```bash
git clone https://github.com/cestbryan-ng/busstationv2-backend.git
cd busstationv2-backend

# Démarrer Redis (Il se démarre généralement automatique après chaque rédemarrage)
# Vérifier bien si le numéro de port est 6379, celui utilisé par Redis

mvn clean install -DskipTests
mvn spring-boot:run

# Swagger
# /swagger-ui.index.html
```
