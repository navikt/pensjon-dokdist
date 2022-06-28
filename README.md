# Pensjon Dokdist

Løsning for sentral print/dokumentdistribusjon

### Bygge og kjøre appen lokalt

- `npm ci`
- `npm run build`
- `mvn clean install`
- Kjør main-metoden til `PensjonDokdistApplication`
- Åpne nettleser `http://localhost:8080`

### Utvikle frontend lokalt, mot lokal backend med autentisering

- Kjør `npm build`. Filer bygges til resources i back-end.
- Start backend lokalt.

- Åpne nettleser: `http://localhost:8080`

Nå vil webpack automatisk oppdatere ressurser (HTML, CSS, JavaScript)
hver gang disse endres. Alle kall mot `http://localhost:8080/api/` er satt opp som en proxy, slik
at disse går mot den "ekte" Spring Boot-backenden som skal kjøre på `http://localhost:8081/api/`.

### Utvikle frontend lokalt, mot en mock-backend

- Start mock-backend: `npm run mock-backend`
  Denne vil nå kjøre på `http://localhost:8081`.
- Kjør `npm run dev` og åpne nettleser: `http://localhost:8080`.
  Alle kall mot `http://localhost:8080/api/` er satt opp som proxy som treffer mock-backenden.
  
### Dokumentasjon via Swagger
- Api dokumentajon: `https://pensjon-dokdist.dev-fss.nais.io/swagger-ui.html`

### Dokumentasjon
`https://pensjon-dokdist.dev-fss.nais.io/site/pensjon-dokdist.html`
  
