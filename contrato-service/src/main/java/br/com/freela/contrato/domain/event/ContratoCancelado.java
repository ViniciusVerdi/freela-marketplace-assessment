package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ContratoCancelado(UUID eventoId, String tipoEvento, Instant ocorridoEm, UUID contratoId,
                                UUID clienteId, UUID freelancerId, String correlationId)
        implements DomainEvent {
    public static ContratoCancelado novo(Contrato c, String correlationId) {
        return new ContratoCancelado(UUID.randomUUID(), "ContratoCancelado", Instant.now(), c.id(), c.clienteId(), c.freelancerId(), correlationId);
    }
}
