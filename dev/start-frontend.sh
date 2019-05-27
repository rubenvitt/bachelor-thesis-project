#!/usr/bin/env bash

cd ../frontend-dev && npm run build
cd ../frontend-server && node bin/www
