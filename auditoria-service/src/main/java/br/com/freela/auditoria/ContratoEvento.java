package br.com.freela.auditoria;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContratoEvento(
    UUID eventoId,
    String tipoEvento,
    Instant ocorridoEm,
    UUID contratoId,
    UUID clienteId,
    UUID freelancerId,
    String titulo,
    BigDecimal valor,
    String correlationId
) {}
