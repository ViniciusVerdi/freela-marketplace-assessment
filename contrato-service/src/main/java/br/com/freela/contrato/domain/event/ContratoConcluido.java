package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContratoConcluido(
    UUID eventoId,
    String tipoEvento,
    Instant ocorridoEm,
    UUID contratoId,
    UUID clienteId,
    UUID freelancerId,
    BigDecimal valor,
    String correlationId
) implements DomainEvent {
    public static ContratoConcluido novo(Contrato c, String correlationId) {
        return new ContratoConcluido(
            UUID.randomUUID(),
            "ContratoConcluido",
            Instant.now(),
            c.id(),
            c.clienteId(),
            c.freelancerId(),
            c.valor(),
            correlationId
        );
    }
}
