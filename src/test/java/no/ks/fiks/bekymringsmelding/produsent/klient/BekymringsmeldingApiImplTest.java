package no.ks.fiks.bekymringsmelding.produsent.klient;

import no.ks.fiks.bekymringsmelding.produsent.klient.model.BekymringsmeldingId;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Historikk;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Krypteringsnokler;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BekymringsmeldingApiImplTest {
    @Mock
    private Maskinportenklient maskinportenklientMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Client clientMock;

    private BekymringsmeldingApiImpl api;

    @BeforeEach
    public void setUp() {
        api = new BekymringsmeldingApiImpl(
                clientMock,
                String.format("http://%s", UUID.randomUUID()),
                maskinportenklientMock,
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
    }

    @Test
    public void bydeler() throws IOException {
        when(clientMock.target(anyString())
                .path(any())
                .path(any())
                .path(any())
                .request(any(MediaType.class))
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .get()
                .readEntity(any(GenericType.class)))
                .thenAnswer((Answer<List<Bydel>>) invocation -> Collections.singletonList(new Bydel()));

        List<Bydel> bydeler = api.getBydeler(UUID.randomUUID().toString());

        assertNotNull(bydeler);
        assertFalse(bydeler.isEmpty());
    }

    @Test
    public void krypteringsnokler() throws IOException {
        when(clientMock.target(anyString())
                .path(any())
                .path(any())
                .path(any())
                .path(any())
                .request(any(MediaType.class))
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .get()
                .readEntity(Krypteringsnokler.class))
                .thenAnswer((Answer<Krypteringsnokler>) invocation -> new Krypteringsnokler());

        Krypteringsnokler krypteringsnokler = api.getKrypteringsnokler(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertNotNull(krypteringsnokler);
    }

    @Test
    public void status() throws IOException {
        when(clientMock.target(anyString())
                .path(any())
                .path(any())
                .path(any())
                .path(any())
                .path(any())
                .request(any(MediaType.class))
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .get()
                .readEntity(any(GenericType.class)))
                .thenAnswer((Answer<List<Historikk>>) invocation -> Collections.singletonList(new Historikk()));

        List<Historikk> status = api.status(UUID.randomUUID());

        assertNotNull(status);
        assertFalse(status.isEmpty());
    }

    @Test
    public void sendBekymringsmelding() {
        when(clientMock.target(anyString())
                .path(any())
                .path(any())
                .path(any())
                .path(any())
                .path(any())
                .request(any(MediaType.class))
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .header(anyString(), anyString())
                .post(any())
                .readEntity((Class<BekymringsmeldingId>) any()))
                .thenAnswer((Answer<BekymringsmeldingId>) invocation -> {
                    BekymringsmeldingId bekymringsmeldingId = new BekymringsmeldingId();
                    bekymringsmeldingId.setUuid(UUID.randomUUID());
                    return bekymringsmeldingId;
                });

        InputStream pdf = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
        InputStream json = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());

        UUID uuid = api.sendBekymringsmelding(UUID.randomUUID().toString(), UUID.randomUUID().toString(), pdf, json);

        assertNotNull(uuid);
    }

}
