package br.com.freela.auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaConsumidor {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaConsumidor.class);

    private final ObjectMapper objectMapper;
    private final AuditoriaService auditoriaService;

    public AuditoriaConsumidor(ObjectMapper objectMapper, AuditoriaService auditoriaService) {
        this.objectMapper = objectMapper;
        this.auditoriaService = auditoriaService;
    }

    @KafkaListener(topics = "contrato.events")
    public void consumir(String mensagem) {
        ContratoEvento evento = desserializar(mensagem);
        if (evento == null) {
            return;
        }
        log.info("auditoria.evento.recebido eventoId={} tipoEvento={} contratoId={} correlationId={}",
                evento.eventoId(), evento.tipoEvento(), evento.contratoId(), evento.correlationId());

        auditoriaService.registrar(
                evento.eventoId(), evento.contratoId(), evento.tipoEvento(),
                evento.correlationId(), mensagem);
    }

    private ContratoEvento desserializar(String mensagem) {
        try {
            return objectMapper.readValue(mensagem, ContratoEvento.class);
        } catch (JsonProcessingException e) {
            log.error("auditoria.evento.desserializacao.falha erro={} mensagem={}", e.getMessage(), mensagem, e);
            return null;
        }
    }
}
