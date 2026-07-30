# syntax=docker/dockerfile:1
FROM gcr.io/distroless/java25-debian13
COPY --exclude=*pensjon-dokdist-*.jar build/install/pensjon-dokdist/lib /app/lib
COPY build/install/pensjon-dokdist/lib/pensjon-dokdist-*.jar /app/lib
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.pensjon.dokdist.DokdistApplicationKt"]