package br.com.freela.notificacao;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_processados")
class EventoProcessado {
    @Id UUID eventoId;
    Instant processadoEm;

    protected EventoProcessado() {}

    EventoProcessado(UUID eventoId) {
        this.eventoId = eventoId;
        this.processadoEm = Instant.now();
    }
}
