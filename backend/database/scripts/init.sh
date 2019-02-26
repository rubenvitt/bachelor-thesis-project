echo "creating init.sql..."

cat /sql/database-init.sql /sql/database-demo.sql > /docker-entrypoint-initdb.d/init.sql
