package br.com.freela.contrato.infrastructure.kafka;

import br.com.freela.contrato.domain.shared.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ProdutorEventosContrato {

    private static final Logger log = LoggerFactory.getLogger(ProdutorEventosContrato.class);
    private static final String TOPICO = "contrato.events";

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProdutorEventosContrato(KafkaTemplate<Object, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public String serializar(DomainEvent evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento " + evento.tipoEvento(), e);
        }
    }

    public void publicar(String chave, String valor) {
        try {
            kafkaTemplate.send(TOPICO, chave, valor).get(10, TimeUnit.SECONDS);
            log.info("contrato.evento.publicado chave={}", chave);
        } catch (Exception e) {
            log.error("contrato.evento.publicacao.falha chave={} erro={}", chave, e.getMessage(), e);
            throw new IllegalStateException("Falha ao publicar evento no Kafka", e);
        }
    }
}
