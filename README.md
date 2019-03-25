# My Business Day

## Installation & Start
#### Create docker-postgres
```bash
cd backend/database && docker build -t database . && docker run -d -p 5432:5432 --name postgres --restart always database
```
#### run database from docker:
```bash
docker run -d -p 5432:5432 --name postgres --restart always docker.fme.de/r.vitt/bsc/database
```
#### build & start backend
```bash
cd backend && mvnw package && cd ..
cd backend/target && java -jar backend-0.0.1-SNAPSHOT.jar
```
#### build & run
```bash
cd frontend-dev && npm install && cd ..
cd frontend-dev && npm run build && cd ..
cd frontend-server && node bin/www

```
