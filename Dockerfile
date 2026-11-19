# syntax=docker/dockerfile:1
FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25
COPY --exclude=*pensjon-dokdist-*.jar build/install/pensjon-dokdist/lib /app/lib
COPY build/install/pensjon-dokdist/lib/pensjon-dokdist-*.jar /app/lib
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.pensjon.dokdist.DokDistApplicationKt"]