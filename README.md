# Bekymringsmelding Produsent Klient
[![MIT Licens](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/ks-no/fiks-bekymringsmelding-produsent-klient/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/no.ks.fiks/fiks-bekymringsmelding-produsent-klient.svg)](https://search.maven.org/search?q=g:no.ks.fiks%20a:fiks-bekymringsmelding-produsent-klient)
[![GitHub Release Date](https://img.shields.io/github/release-date/ks-no/fiks-bekymringsmelding-produsent-klient.svg)](https://github.com/ks-no/fiks-bekymringsmelding-produsent-klient/releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/ks-no/fiks-bekymringsmelding-produsent-klient.svg)](https://github.com/ks-no/fiks-bekymringsmelding-produsent-klient/commits/master)

Java-klient for å laste opp bekymringsmelding til Fiks-Bekymringsmelding.
```java
        BekymringsmeldingKlient klient = new BekymringsmeldingKlient(api, asicHandler, cmsKryptering);

        // Finn bydeler tilhørende kommunen som skal ha bekymringsmelding
        String kommunenummer = ...;
        List<Bydel> bydeler = api.getBydeler(kommunenummer);
        if (bydeler.isEmpty()) {
            throw new RuntimeException("Kommunen er ikke konfigurert til å motta bekymringsmelding via Fiks-plattformen");
        }
        // Velg bydel, eventuelt send til standard bydel
        Bydel bydel = bydeler.stream().filter(Bydel::isStandardMottaker).findFirst().orElse(bydeler.get(0));

        // PDF og JSON som InputStream
        InputStream ukryptertBekymringsmeldingPdf = ...; //bekymringsmelding.pdf
        InputStream ukryptertBekymringsmeldingJson = ...; //bekymringsmelding.json

        UUID bekymringsmeldingId = klient.krypterOgSendBekymringsmelding(kommunenummer, bydel.getBydelsnummer(), ukryptertBekymringsmeldingPdf, ukryptertBekymringsmeldingJson);

        List<Historikk> status = api.status(bekymringsmeldingId);

        System.out.println("Historikk på bekymringsmelding " + bekymringsmeldingId);
        status.forEach(System.out::println);

```
Komplett eksempel
```java
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Historikk;
import no.ks.fiks.io.asice.AsicHandler;
import no.ks.fiks.io.asice.AsicHandlerBuilder;
import no.ks.fiks.io.asice.model.KeystoreHolder;
import no.ks.fiks.maskinporten.Maskinportenklient;
import no.ks.fiks.maskinporten.MaskinportenklientProperties;
import no.ks.kryptering.CMSKrypteringImpl;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class POC {
    public static void main(String[] args) throws Exception {
        // Verdier hentes fra Fiks-plattformen
        String baseUrl = "https://api.fiks.test.ks.no/bekymringsmelding";

        UUID fiksOrdId = UUID.fromString("");
        String integrationId = "";
        String integrationPassord = "";

        // Verdier hentes fra DigDir
        String maskinportenklientid = "";
        String audience = "https://ver2.maskinporten.no/";
        String tokenEndpoint = "https://ver2.maskinporten.no/token";

        // Verdier hentes fra virksomhetssertifikat
        String keyStoreFilename = "";
        String keyStorePassword = "";
        String privateKeyAlias = "";

        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new FileInputStream(keyStoreFilename), keyStorePassword.toCharArray());

        Maskinportenklient maskinportenklient = new Maskinportenklient(
                keyStore,
                privateKeyAlias,
                keyStorePassword.toCharArray(),
                MaskinportenklientProperties.builder()
                        .numberOfSecondsLeftBeforeExpire(10)
                        .issuer(maskinportenklientid)
                        .audience(audience)
                        .tokenEndpoint(tokenEndpoint)
                        .build());

        Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        BekymringsmeldingApi api = new BekymringsmeldingApiImpl(
                client,
                baseUrl,
                maskinportenklient,
                fiksOrdId,
                integrationId,
                integrationPassord
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AsicHandler asicHandler = AsicHandlerBuilder.create()
                .withExecutorService(executorService)
                .withKeyStoreHolder(KeystoreHolder.builder()
                        .withKeyAlias(privateKeyAlias)
                        .withKeyPassword(keyStorePassword)
                        .withKeyStorePassword(keyStorePassword)
                        .withKeyStore(keyStore)
                        .build())
                .build();

        CMSKrypteringImpl cmsKryptering = new CMSKrypteringImpl();

        BekymringsmeldingKlient klient = new BekymringsmeldingKlient(api, asicHandler, cmsKryptering);

        // Finn bydeler tilhørende kommunen som skal ha bekymringsmelding
        String kommunenummer = ...;
        List<Bydel> bydeler = api.getBydeler(kommunenummer);
        if (bydeler.isEmpty()) {
            throw new RuntimeException("Kommunen er ikke konfigurert til å motta bekymringsmelding via Fiks-plattformen");
        }
        // Velg bydel, eventuelt send til standard bydel
        Bydel bydel = bydeler.stream().filter(Bydel::isStandardMottaker).findFirst().orElse(bydeler.get(0));

        // PDF og JSON som InputStream
        InputStream ukryptertBekymringsmeldingPdf = ...; //bekymringsmelding.pdf
        InputStream ukryptertBekymringsmeldingJson = ...; //bekymringsmelding.json

        UUID bekymringsmeldingId = klient.krypterOgSendBekymringsmelding(kommunenummer, bydel.getBydelsnummer(), ukryptertBekymringsmeldingPdf, ukryptertBekymringsmeldingJson);

        List<Historikk> status = api.status(bekymringsmeldingId);

        System.out.println("Historikk på bekymringsmelding " + bekymringsmeldingId);
        status.forEach(System.out::println);

        System.out.println("Nedstenging av applikasjon");
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        asicHandler.close();
        client.close();
    }
}
```
