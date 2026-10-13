package br.com.freela.contrato.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_eventos")
public class EventoOutbox {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private UUID eventoId;
    @Column(nullable = false) private String tipoEvento;
    private String correlationId;
    @Column(nullable = false) private UUID contratoId;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private boolean publicado;
    private Instant criadoEm;
    private Instant publicadoEm;

    protected EventoOutbox() {}

    public EventoOutbox(UUID eventoId, String tipoEvento, String correlationId, UUID contratoId, String payload) {
        this.id = UUID.randomUUID();
        this.eventoId = eventoId;
        this.tipoEvento = tipoEvento;
        this.correlationId = correlationId;
        this.contratoId = contratoId;
        this.payload = payload;
        this.publicado = false;
        this.criadoEm = Instant.now();
    }

    public UUID getEventoId() { return eventoId; }
    public String getTipoEvento() { return tipoEvento; }
    public String getCorrelationId() { return correlationId; }
    public UUID getContratoId() { return contratoId; }
    public String getPayload() { return payload; }
    public boolean isPublicado() { return publicado; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getPublicadoEm() { return publicadoEm; }

    public void marcarPublicado() {
        this.publicado = true;
        this.publicadoEm = Instant.now();
    }
}
