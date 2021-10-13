package no.ks.fiks.bekymringsmelding.produsent.klient.model;

import lombok.Data;

@Data
public class Krypteringsnokler {
    private Nokkel printOffentligNokkel;
    private Nokkel fiksIOOffentligNokkel;
}
