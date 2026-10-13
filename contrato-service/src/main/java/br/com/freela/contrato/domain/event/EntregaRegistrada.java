package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record EntregaRegistrada(
    UUID eventoId,
    String tipoEvento,
    Instant ocorridoEm,
    UUID contratoId,
    UUID clienteId,
    UUID freelancerId,
    String titulo,
    String correlationId
) implements DomainEvent {
    public static EntregaRegistrada nova(Contrato c, String correlationId) {
        return new EntregaRegistrada(
            UUID.randomUUID(),
            "EntregaRegistrada",
            Instant.now(),
            c.id(),
            c.clienteId(),
            c.freelancerId(),
            c.titulo(),
            correlationId
        );
    }
}
