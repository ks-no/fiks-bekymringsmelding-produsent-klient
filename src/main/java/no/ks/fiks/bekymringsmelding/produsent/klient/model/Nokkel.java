package no.ks.fiks.bekymringsmelding.produsent.klient.model;

import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

@Data
public class Nokkel {
    private String nokkel;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String serial;
    private String subjectDN;
    private String issuerDN;

    public X509Certificate getX509Certificate() {
        try {
            InputStream targetStream = new ByteArrayInputStream(nokkel.getBytes());
            return (X509Certificate) CertificateFactory
                    .getInstance("X509")
                    .generateCertificate(targetStream);
        } catch (CertificateException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
