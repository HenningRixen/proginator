# proginator
Projektmanagement Projekt


# Lokal

Rechts oben in IntelliJ → Run Configuration deiner App auswählen.
Reiter “Configuration”.
Feld “VM options” suchen.
Folgendes eintragen:
-Dspring.profiles.active=dev

# docker
## Docker Container bauen prod für database:

docker run --name lernapp-postgres \
-e POSTGRES_DB=lernapp \
-e POSTGRES_USER=lernapp \
-e POSTGRES_PASSWORD=secret \
-p 5432:5432 \
-d postgres:16

## Docker starten und stoppen

docker stop lernapp-postgres
docker start lernapp-postgres  

## Docker löschen um frische datenbank aufzusetzen danach

docker rm -f lernapp-postgres


## Um in die datenbank zu schauen braucht man entweder intelij ultimate oder man kann es über psql machen

psql -h localhost -U lernapp -d lernapp

und dann über SELECT * FROM public.users; einmal die user tabelle ausgeben lassen




#docker auf server laufen lassen:
# Starten (im Hintergrund)
docker compose -f docker-compose.prod.yml up -d

# Logs ansehen
docker compose -f docker-compose.prod.yml logs -f

# Stoppen
docker compose -f docker-compose.prod.yml down

#nginx restarten
sudo pkill -9 nginx
sudo systemctl stop nginx
sudo systemctl disable nginx

#nginx already in use
sudo lsof -i :80

#jar bauen
./mvnw clean package

