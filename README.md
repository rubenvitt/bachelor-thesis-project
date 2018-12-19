# My Business Day

## Installation & Start
#### Create docker-postgres:
```bash
docker run -d -e POSTGRES_PASSWORD=postgres --name "postgres" -p 5432:5432 postgres
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