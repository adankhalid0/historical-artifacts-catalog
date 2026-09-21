# Historical Artifacts Catalog

Et objektorientert Java-prosjekt om registrering av historiske gjenstander: en gruppe hobby-arkeologer har samlet funn over tid, og programmet importerer rådata fra en tekstfil inn i en database, og lar deg deretter utforske funnene gjennom en enkel kommandolinje-meny.

> **Om opphavet:** Caset er hentet fra en individuell hjemmeeksamen i PGR112 (Objektorientert programmering, Høyskolen Kristiania, V2024). Denne løsningen er skrevet på nytt i ettertid, uavhengig av den opprinnelige eksamensbesvarelsen, som portefølje-eksempel på arv, polymorfi, innkapsling, unntakshåndtering, JDBC og filhåndtering.

## Caset

Informasjon om personer, museer og funngjenstander ligger i [`data/funn.txt`](data/funn.txt) i et fast tekstformat. Det finnes tre typer funngjenstander (mynt, våpen, smykke) som deler de fleste egenskapene (koordinater, finner, funndato, antatt årstall, ev. museum) og har noen få egne (en mynt har diameter og metall, et våpen har type/materiale/vekt, et smykke har type/verdiestimat/bildefil).

Programmet gjør to ting:

1. **Import** — leser `funn.txt` og lagrer alt i en database.
2. **Meny** — lar deg se alle funn, se funn eldre enn et gitt årstall, og telle antall registrerte funn.

## Design

- **`Funngjenstand`** er en abstrakt klasse som `Mynt`, `Vapen` og `Smykke` arver fra. Delte felter og validering ligger i overklassen; hver underklasse legger til sine egne felter.
- **`Rapporterbar`** er et interface med én metode (`tilRapportLinje()`), implementert av `Funngjenstand` som en *template method* — den setter sammen den delte delen av rapportlinjen selv og kaller to abstrakte metoder underklassene fyller ut. Del 2 av programmet skriver ut funn uten å sjekke hvilken konkret type hvert objekt er.
- Modellklassene er strengt innkapslet: private, for det meste `final`-felter, med validering i konstruktørene (kaster `UgyldigFunnDataException` ved ugyldige verdier). Det eneste feltet som kan endres etter opprettelse — hvilket museum et funn hører til — har en egen validert setter.
- **`FunnRepository`** er den eneste klassen som snakker JDBC; resten av programmet ser bare vanlige Java-objekter. Databasen er strukturert som *table-per-subclass* (se [`funn.sql`](src/main/resources/funn.sql)), som speiler Java-arvehierarkiet direkte.
- Databasen er **SQLite** — én lokal fil, ingen databaseserver å installere. Det var et bevisst valg for at prosjektet skal kunne kjøres rett i IntelliJ uten noe ekstra oppsett.

Se [`rapport/PGR112_Rapport.docx`](rapport/PGR112_Rapport.docx) for en fullstendig gjennomgang av unntakshåndtering, innkapsling, arv og forutsetningene som er lagt til grunn.

## Kjøre prosjektet

**I IntelliJ:** åpne mappen som et Maven-prosjekt (IntelliJ finner `pom.xml` automatisk), og kjør `Main.java`. Programmet importerer `data/funn.txt` til en lokal `funn.db`-fil og åpner menyen.

**I terminal:**

```bash
mvn package
java -jar target/historical-artifacts-catalog.jar
```

## Teknologi

`Java 17` · `JDBC` · `SQLite` · `Maven`
