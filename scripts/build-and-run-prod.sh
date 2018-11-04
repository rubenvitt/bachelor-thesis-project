#!/usr/bin/env bash

echo "[INFO] You need npm and nodeJS installed to run this script";

./build-project.sh
cd ../frontend-server
clear
node bin/www
