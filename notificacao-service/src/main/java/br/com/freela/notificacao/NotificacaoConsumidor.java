package br.com.freela.notificacao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoConsumidor {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoConsumidor.class);

    private final ObjectMapper objectMapper;
    private final NotificacaoService notificacaoService;

    public NotificacaoConsumidor(ObjectMapper objectMapper, NotificacaoService notificacaoService) {
        this.objectMapper = objectMapper;
        this.notificacaoService = notificacaoService;
    }

    @KafkaListener(topics = "contrato.events")
    public void consumir(String mensagem) {
        ContratoEvento evento = desserializar(mensagem);
        if (evento == null) {
            return;
        }
        log.info("notificacao.evento.recebido eventoId={} tipoEvento={} contratoId={} correlationId={}",
                evento.eventoId(), evento.tipoEvento(), evento.contratoId(), evento.correlationId());

        switch (evento.tipoEvento()) {
            case "ContratoCriado" -> notificacaoService.registrar(
                    evento.eventoId(), evento.contratoId(), evento.freelancerId(), evento.tipoEvento(),
                    "Contrato criado: " + evento.titulo());
            case "EntregaRegistrada" -> notificacaoService.registrar(
                    evento.eventoId(), evento.contratoId(), evento.clienteId(), evento.tipoEvento(),
                    "Entrega registrada no contrato");
            case "ContratoConcluido" -> notificacaoService.registrar(
                    evento.eventoId(), evento.contratoId(), evento.freelancerId(), evento.tipoEvento(),
                    "Contrato concluído");
            case "ContratoCancelado" -> notificacaoService.registrar(
                    evento.eventoId(), evento.contratoId(), evento.freelancerId(), evento.tipoEvento(),
                    "Contrato cancelado");
            default -> log.warn("notificacao.evento.ignorado tipoEvento={} contratoId={}",
                    evento.tipoEvento(), evento.contratoId());
        }
    }

    private ContratoEvento desserializar(String mensagem) {
        try {
            return objectMapper.readValue(mensagem, ContratoEvento.class);
        } catch (JsonProcessingException e) {
            log.error("Erro: notificacao.evento.desserializacao.falha erro={} mensagem={}",
                    e.getMessage(), mensagem, e);
            return null;
        }
    }
}
