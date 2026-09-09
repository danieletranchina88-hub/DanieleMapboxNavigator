# Daniele Mapbox Navigator

Navigatore Android personale basato su Mapbox Navigation SDK. Usa la posizione reale del telefono e offre un'esperienza turn-by-turn completa in italiano.

## Funzioni incluse (v0.2.0)

- GPS reale con map matching sulla strada.
- Barra di ricerca Mapbox con suggerimenti per indirizzi, luoghi e punti di interesse.
- Selezione diretta del risultato e calcolo immediato del percorso.
- Selezione della destinazione con pressione prolungata sulla mappa.
- Stile Mapbox Standard: la stessa estetica e precisione cartografica di Mapbox, con
  edifici 3D fotorealistici (ombre e luce in tempo reale), landmark 3D, attraversamenti
  pedonali e resa stradale nativi, senza livelli personalizzati "fatti a mano".
- Rilievo del terreno in 3D (Mapbox Terrain-DEM) per colline e dislivelli reali, e
  percorso colorato in base al traffico reale (verde/giallo/rosso) durante la guida.
- Visuale inclinata e pulsante rapido per passare tra mappa 2D (piatta) e 3D (edifici in
  rilievo con illuminazione realistica).
- Indicatore grafico della destinazione sulla mappa.
- Interfaccia personalizzata con barra flottante, pannello risultati e comandi compatti.
- Percorso automobilistico con traffico e ricalcolo automatico fuori rotta.
- Linea del percorso aggiornata durante la guida e freccia della prossima manovra.
- Istruzioni grafiche e vocali in italiano.
- Tempo di arrivo, tempo residuo e distanza residua.
- Comandi per ricentrare la mappa, vedere l'intero percorso, silenziare la voce e terminare la navigazione.
- Preset di luce Mapbox Standard (giorno/notte) automatico in base al tema del telefono.
- Supporto Android 6.0+ (`minSdk 23`) e dispositivi Android con pagine di memoria da 16 KB.
- Workflow GitHub Actions manuale per produrre un APK di debug scaricabile.

## Requisiti

- Android Studio Quail 4 (2026.1.4) o compatibile.
- JDK 17.
- Account Mapbox.
- Un token Mapbox pubblico (`pk...`).
- Un token Mapbox segreto (`sk...`) con ambito **Downloads:Read**.

La configurazione usa Mapbox Navigation SDK `3.30.0`, Mapbox Search SDK `2.30.0`, Android Gradle Plugin `9.4.0`, Gradle `9.6.0`, `compileSdk 37` e `targetSdk 36`.

## Configurazione locale

Non inserire mai i token nei file versionati del repository.

Nel file globale `~/.gradle/gradle.properties` aggiungi:

```properties
MAPBOX_DOWNLOADS_TOKEN=<TOKEN_SEGRETO_MAPBOX>
MAPBOX_PUBLIC_TOKEN=<TOKEN_PUBBLICO_MAPBOX>
```

Poi apri il progetto in Android Studio, sincronizza Gradle e avvia l'app su un telefono Android reale. Per la navigazione servono posizione precisa, connessione dati e notifiche.

## Generare l'APK su GitHub

Nel repository apri **Settings → Secrets and variables → Actions** e crea questi due repository secrets:

| Nome | Valore |
|---|---|
| `MAPBOX_DOWNLOADS_TOKEN` | Token segreto Mapbox con `Downloads:Read` |
| `MAPBOX_PUBLIC_TOKEN` | Token pubblico Mapbox che inizia con `pk.` |

Apri quindi **Actions → Build Android APK → Run workflow**. A build terminata, scarica l'artifact **Daniele-Navigator-debug**.

## Uso

1. Concedi posizione precisa e notifiche.
2. Attendi il messaggio che conferma il GPS pronto.
3. Cerca un indirizzo o un luogo e selezionalo dai suggerimenti. In alternativa, tieni premuto sulla mappa.
4. Attendi il calcolo del percorso e inizia a guidare.
5. Usa il pulsante `2D/3D` per cambiare prospettiva e il pulsante rosso per terminare la navigazione.

> Sicurezza: non interagire con il telefono durante la guida. Imposta la destinazione prima di partire e rispetta sempre la segnaletica stradale.

## Privacy e costi

La posizione e le richieste di percorso vengono elaborate dai servizi Mapbox necessari al funzionamento del navigatore. Il progetto non contiene un backend proprio e non salva una cronologia personale dei viaggi. Consulta [PRIVACY.md](PRIVACY.md).

Mapbox applica le condizioni e le soglie del piano associato all'account. Verifica sempre la [pagina prezzi ufficiale Mapbox](https://www.mapbox.com/pricing/) prima di un uso esteso.

## Roadmap

- Preferiti Casa e Lavoro.
- Percorsi alternativi.
- Limiti di velocità e avvisi.
- Punti di riferimento 3D avanzati.
- Android Auto.
- Cache predittiva e mappe offline.
- Interfaccia personalizzata in stile infotainment automobilistico.

## Documentazione

- [Installazione Navigation SDK](https://docs.mapbox.com/android/navigation/guides/install/)
- [Documentazione Navigation SDK Android](https://docs.mapbox.com/android/navigation/)

## Licenza

Codice distribuito con licenza MIT. Mapbox e i relativi SDK restano soggetti alle rispettive condizioni di utilizzo.
