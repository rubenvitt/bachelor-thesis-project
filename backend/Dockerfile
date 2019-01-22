FROM openjdk:11-slim
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
EXPOSE 8443
COPY src/main/resources/keystore.jks /
ENTRYPOINT ["java","-cp","app:app/lib/*","de.rubeen.bsc.BackendApplication"]