#!/usr/bin/env bash


export DB_HOST=localhost
./start-db.sh
cd ../backend && mvn spring-boot:run
