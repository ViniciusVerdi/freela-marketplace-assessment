package br.com.freela.contrato.domain.shared;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventoId();
    Instant ocorridoEm();
    String tipoEvento();
    String correlationId();
    UUID contratoId();
}
