package no.ks.fiks.bekymringsmelding.produsent.klient.model;

import lombok.Data;

@Data
public class Bydel {
    private String bydelsnummer;
    private String bydelsnavn;
    private boolean standardMottaker;
}
