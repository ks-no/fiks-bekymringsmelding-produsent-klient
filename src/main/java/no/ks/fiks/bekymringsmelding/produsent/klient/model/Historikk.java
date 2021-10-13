package no.ks.fiks.bekymringsmelding.produsent.klient.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Historikk {
    private UUID bekymringsmeldingId;
    private LocalDateTime tidspunkt;
    private String tilstand;
    private String beskrivelse;
}
