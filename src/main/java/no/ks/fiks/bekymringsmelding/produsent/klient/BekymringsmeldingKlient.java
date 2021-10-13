package no.ks.fiks.bekymringsmelding.produsent.klient;

import lombok.extern.slf4j.Slf4j;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Bydel;
import no.ks.fiks.bekymringsmelding.produsent.klient.model.Krypteringsnokler;
import no.ks.fiks.io.asice.AsicHandler;
import no.ks.fiks.io.asice.model.StreamContent;
import no.ks.kryptering.CMSStreamKryptering;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
public class BekymringsmeldingKlient {
    private final BekymringsmeldingApi api;
    private final AsicHandler asicHandler;
    private final CMSStreamKryptering cmsStreamKryptering;

    public BekymringsmeldingKlient(BekymringsmeldingApi api, AsicHandler asicHandler, CMSStreamKryptering cmsStreamKryptering) {
        this.api = api;
        this.asicHandler = asicHandler;
        this.cmsStreamKryptering = cmsStreamKryptering;
    }

    public UUID krypterOgSendBekymringsmelding(String kommunenummer, InputStream bekymringsmeldingPdf, InputStream bekymringsmeldingJson) {
        return krypterOgSendBekymringsmelding(kommunenummer, api.getBydeler(kommunenummer).stream().filter(Bydel::isStandardMottaker).findFirst().orElseThrow(() -> new RuntimeException("Kommunen har ingen standard mottaker. Angi bydel som skal motta bekymringsmeldingen")).getBydelsnummer(), bekymringsmeldingPdf, bekymringsmeldingJson);
    }

    public UUID krypterOgSendBekymringsmelding(String kommunenummer, String bydelsnummer, InputStream bekymringsmeldingPdf, InputStream bekymringsmeldingJson) {
        Krypteringsnokler krypteringsnokler = api.getKrypteringsnokler(kommunenummer, bydelsnummer);

        ByteArrayOutputStream encryptedBekymringsmeldingPdf = new ByteArrayOutputStream();
        try {
            byte[] bekymringsmeldingPdfAsByte = IOUtils.toByteArray(bekymringsmeldingPdf);
            cmsStreamKryptering.krypterData(encryptedBekymringsmeldingPdf, new ByteArrayInputStream(bekymringsmeldingPdfAsByte), krypteringsnokler.getPrintOffentligNokkel().getX509Certificate());
            ByteArrayInputStream bekymringsmeldingPdfInputStream = new ByteArrayInputStream(encryptedBekymringsmeldingPdf.toByteArray());
            return api.sendBekymringsmelding(
                    kommunenummer,
                    bydelsnummer,
                    bekymringsmeldingPdfInputStream,
                    asicHandler.encrypt(krypteringsnokler.getFiksIOOffentligNokkel().getX509Certificate(), Arrays.asList(new StreamContent(bekymringsmeldingJson, "bekymringsmelding.json"), new StreamContent(new ByteArrayInputStream(bekymringsmeldingPdfAsByte), "bekymringsmelding.pdf")))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(encryptedBekymringsmeldingPdf);
            close(bekymringsmeldingJson);
            close(bekymringsmeldingPdf);
        }
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            log.warn("Klarte ikke å lukke ressurs!", e);
        }
    }
}
