package no.ks.fiks.bekymringsmelding.produsent.klient;

import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Krypteringsnokler;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Nokkel;
import no.ks.fiks.io.asice.AsicHandler;
import no.ks.kryptering.CMSStreamKryptering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BekymringsmeldingKlientTest {

    @Mock
    private BekymringsmeldingApi apiMock;
    @Mock
    private AsicHandler asicHandlerMock;
    @Mock
    private CMSStreamKryptering cmsStreamKrypteringMock;
    @Mock
    private Krypteringsnokler krypteringsnoklerMock;
    @Mock
    private Nokkel nokkelMock;
    @Mock
    private Bydel bydelMock;
    private BekymringsmeldingKlient klient;
    private InputStream pdf;
    private InputStream json;

    @BeforeEach
    public void setUp() {
        pdf = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
        json = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
        klient = new BekymringsmeldingKlient(apiMock, asicHandlerMock, cmsStreamKrypteringMock);
    }

    @Test
    public void krypterBekymringsmelding_medBydel_skalReturnereBekymringsmeldingsid() throws IOException {
        when(apiMock.getKrypteringsnokler(anyString(), anyString())).thenAnswer((Answer<Krypteringsnokler>) invocation -> krypteringsnoklerMock);
        when(apiMock.sendBekymringsmelding(anyString(), anyString(), any(), any())).thenAnswer((Answer<UUID>) invocation -> UUID.randomUUID());
        when(krypteringsnoklerMock.getPrintOffentligNokkel()).thenAnswer((Answer<Nokkel>) invocation -> nokkelMock);
        when(krypteringsnoklerMock.getFiksIOOffentligNokkel()).thenAnswer((Answer<Nokkel>) invocation -> nokkelMock);

        UUID bekymringsmeldingsid = klient.krypterOgSendBekymringsmelding("kommunenummer", "bydelsnummer", pdf, json);

        assertNotNull(bekymringsmeldingsid);
    }

    @Test
    public void krypterBekymringsmelding_utenBydelSkalBrukeStandardMottaker_skalReturnereBekymringsmeldingsid() throws IOException {
        when(apiMock.getKrypteringsnokler(anyString(), anyString())).thenAnswer((Answer<Krypteringsnokler>) invocation -> krypteringsnoklerMock);
        when(apiMock.sendBekymringsmelding(anyString(), anyString(), any(), any())).thenAnswer((Answer<UUID>) invocation -> UUID.randomUUID());
        when(krypteringsnoklerMock.getPrintOffentligNokkel()).thenAnswer((Answer<Nokkel>) invocation -> nokkelMock);
        when(krypteringsnoklerMock.getFiksIOOffentligNokkel()).thenAnswer((Answer<Nokkel>) invocation -> nokkelMock);
        when(apiMock.getBydeler(anyString())).thenAnswer((Answer<List<Bydel>>) invocation -> Collections.singletonList(bydelMock));
        when(bydelMock.isStandardMottaker()).thenAnswer((Answer<Boolean>) invocation -> true);
        when(bydelMock.getBydelsnummer()).thenAnswer((Answer<String>) invocation -> UUID.randomUUID().toString());

        UUID bekymringsmeldingsid = klient.krypterOgSendBekymringsmelding("kommunenummer", pdf, json);

        assertNotNull(bekymringsmeldingsid);
    }

    @Test
    public void krypterBekymringsmeldingManglerStandardMottaker_utenBydelSkalBrukeStandardMottaker_skalFeile() throws IOException {
        assertThrows(RuntimeException.class, () -> klient.krypterOgSendBekymringsmelding("kommunenummer", pdf, json));
    }
}
