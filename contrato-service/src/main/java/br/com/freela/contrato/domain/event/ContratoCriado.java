package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContratoCriado(
    UUID eventoId,
    String tipoEvento,
    Instant ocorridoEm,
    UUID contratoId,
    UUID clienteId,
    UUID freelancerId,
    String titulo,
    BigDecimal valor,
    String correlationId
) implements DomainEvent {
    public static ContratoCriado novo(Contrato c, String correlationId) {
        return new ContratoCriado(
            UUID.randomUUID(),
            "ContratoCriado",
            Instant.now(),
            c.id(),
            c.clienteId(),
            c.freelancerId(),
            c.titulo(),
            c.valor(),
            correlationId
        );
    }
}
