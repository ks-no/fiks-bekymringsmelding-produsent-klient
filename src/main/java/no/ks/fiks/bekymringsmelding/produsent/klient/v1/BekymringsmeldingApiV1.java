package no.ks.fiks.bekymringsmelding.produsent.klient.v1;

import no.ks.fiks.bekymringsmelding.produsent.klient.BekymringsmeldingApi;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Historikk;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Krypteringsnokler;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface BekymringsmeldingApiV1 extends BekymringsmeldingApi {

    List<Bydel> getBydeler(String kommunenummer);

    Krypteringsnokler getKrypteringsnokler(String kommunenummer, String bydelsnummer);

    UUID sendBekymringsmelding(String kommunenummer, String bydelsnummer, InputStream bekymringsmeldingPdfInputStream, InputStream bekymringsmeldingAsiceInputStream);

    List<Historikk> status(UUID bekymringsmeldingId);
}
