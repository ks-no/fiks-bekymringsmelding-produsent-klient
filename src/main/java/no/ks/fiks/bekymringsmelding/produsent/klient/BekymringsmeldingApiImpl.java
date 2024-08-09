package no.ks.fiks.bekymringsmelding.produsent.klient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import lombok.NonNull;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.BekymringsmeldingId;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Historikk;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Krypteringsnokler;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class BekymringsmeldingApiImpl implements BekymringsmeldingApi {
    private final Client client;
    private final String baseUrl;
    private final Maskinportenklient maskinporten;
    private final UUID fiksOrgId;
    private final String integrasjonId;
    private final String integrasjonPassord;

    public BekymringsmeldingApiImpl(Client client, String baseUrl, Maskinportenklient maskinporten, UUID fiksOrgId, String integrasjonId, String integrasjonPassord) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.maskinporten = maskinporten;
        this.fiksOrgId = fiksOrgId;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    @Override
    public List<Bydel> getBydeler(@NonNull String kommunenummer) {
        return client.target(baseUrl)
                .path("api/v1/kommuner")
                .path(kommunenummer)
                .path("bydeler")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("IntegrasjonId", integrasjonId)
                .header("IntegrasjonPassord", integrasjonPassord)
                .header("Authorization", String.format("Bearer %s", maskinporten.getAccessToken("ks:fiks")))
                .get()
                .readEntity(new GenericType<List<Bydel>>() {});
    }

    @Override
    public Krypteringsnokler getKrypteringsnokler(@NonNull String kommunenummer, @NonNull String bydelsnummer) {
        return client.target(baseUrl)
                .path("api/v1/mottak/fagsystem")
                .path(kommunenummer)
                .path(bydelsnummer)
                .path("krypteringsnokler")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("IntegrasjonId", integrasjonId)
                .header("IntegrasjonPassord", integrasjonPassord)
                .header("Authorization", String.format("Bearer %s", maskinporten.getAccessToken("ks:fiks")))
                .get()
                .readEntity(Krypteringsnokler.class);
    }

    @Override
    public UUID sendBekymringsmelding(@NonNull String kommunenummer, @NonNull String bydelsnummer, @NonNull InputStream bekymringsmeldingPdfInputStream, @NonNull InputStream bekymringsmeldingAsiceInputStream) {
        StreamDataBodyPart bekymringsmeldingPdf = new StreamDataBodyPart("bekymringsmelding", bekymringsmeldingPdfInputStream, "bekymringsmelding.pdf");
        StreamDataBodyPart asiceZip = new StreamDataBodyPart("asice", bekymringsmeldingAsiceInputStream, "asice.zip");

        MultiPart multipart = new FormDataMultiPart().bodyPart(bekymringsmeldingPdf).bodyPart(asiceZip);

        return client.target(baseUrl)
                .path("/api/v1/mottak/fagsystem")
                .path(fiksOrgId.toString())
                .path(kommunenummer)
                .path(bydelsnummer)
                .path("offentlig")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("IntegrasjonId", integrasjonId)
                .header("IntegrasjonPassord", integrasjonPassord)
                .header("Authorization", String.format("Bearer %s", maskinporten.getAccessToken("ks:fiks")))
                .post(Entity.entity(multipart, multipart.getMediaType()))
                .readEntity(BekymringsmeldingId.class).getUuid();
    }

    @Override
    public List<Historikk> status(@NonNull UUID bekymringsmeldingId) {
        return client.target(baseUrl)
                .path("api/v1/mottak/fagsystem")
                .path(fiksOrgId.toString())
                .path("bekymringsmelding")
                .path(bekymringsmeldingId.toString())
                .path("status")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("IntegrasjonId", integrasjonId)
                .header("IntegrasjonPassord", integrasjonPassord)
                .header("Authorization", String.format("Bearer %s", maskinporten.getAccessToken("ks:fiks")))
                .get()
                .readEntity(new GenericType<List<Historikk>>() {});
    }
}
