package br.com.freela.contrato.infrastructure.kafka;

import br.com.freela.contrato.infrastructure.persistence.EventoOutbox;
import br.com.freela.contrato.infrastructure.persistence.EventoOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final EventoOutboxRepository outboxRepository;
    private final ProdutorEventosContrato produtorEventos;

    public OutboxRelay(EventoOutboxRepository outboxRepository, ProdutorEventosContrato produtorEventos) {
        this.outboxRepository = outboxRepository;
        this.produtorEventos = produtorEventos;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:2000}")
    public void processarPendentes() {
        List<EventoOutbox> pendentes = outboxRepository.findByPublicadoFalse();
        for (EventoOutbox pendente : pendentes) {
            try {
                produtorEventos.publicar(pendente.getContratoId().toString(), pendente.getPayload());
                pendente.marcarPublicado();
                outboxRepository.save(pendente);
                log.info("outbox.publicado eventoId={} tipoEvento={} contratoId={}",
                        pendente.getEventoId(), pendente.getTipoEvento(), pendente.getContratoId());
            } catch (Exception e) {
                log.error("outbox.publicacao.falha eventoId={} tipoEvento={} erro={}",
                        pendente.getEventoId(), pendente.getTipoEvento(), e.getMessage(), e);
            }
        }
    }
}
