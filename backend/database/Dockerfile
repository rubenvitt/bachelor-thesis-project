FROM postgres

ENV POSTGRES_USER testuser
ENV POSTGRES_PASSWORD testpass
ENV POSTGRES_DB testdatabase

RUN echo "copy all sql files and shell-scripts to /sql"
COPY scripts/*.sql /sql/
COPY scripts/init.sh /sql/

RUN echo "run init-script..."
RUN chmod +x /sql/init.sh && /sql/init.sh

EXPOSE 5432
