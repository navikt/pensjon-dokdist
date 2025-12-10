# Pensjon Dokdist

Løsning for sentral print/dokumentdistribusjon.

Må kjøres med spring profilen 'local' om det kjøres lokalt, eller profilen 'nais' om det kjører i NAIS.
For å kunne kjøre backend lokalt må man først kjøre `/fetch-secrets.sh` for å hente nødvendige hemmeligheter.

### Bygge og kjøre appen lokalt

- `npm ci`
- `npm run build`
- `mvn clean install`
- Kjør Spring Boot applikasjonen med spring profil 'local' `PensjonDokdistApplication`
- Åpne nettleser `http://localhost:8080`

### Utvikle frontend lokalt, mot en mock-backend

- Start mock-backend: `npm run mock-backend`
  Denne vil nå kjøre på `http://localhost:8081`.
- Kjør `npm run dev` og åpne nettleser: `http://localhost:8080`.
  Alle kall mot `http://localhost:8080/api/` er satt opp som proxy som treffer mock-backenden.
  
### Dokumentasjon via Swagger
- Api dokumentajon: `https://pensjon-dokdist.intern.dev.nav.no/swagger-ui.html`

### Dokumentasjon
`https://navikt.github.io/pensjon-dokdist`
  
