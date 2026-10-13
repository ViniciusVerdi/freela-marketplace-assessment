package br.com.freela.reputacao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReputacaoConsumidor {

    private static final Logger log = LoggerFactory.getLogger(ReputacaoConsumidor.class);

    private final ObjectMapper objectMapper;
    private final ReputacaoService reputacaoService;

    public ReputacaoConsumidor(ObjectMapper objectMapper, ReputacaoService reputacaoService) {
        this.objectMapper = objectMapper;
        this.reputacaoService = reputacaoService;
    }

    @KafkaListener(topics = "contrato.events")
    public void consumir(String mensagem) {
        ContratoEvento evento = desserializar(mensagem);
        if (evento == null) {
            return;
        }
        log.info("reputacao.evento.recebido eventoId={} tipoEvento={} contratoId={} correlationId={}",
                evento.eventoId(), evento.tipoEvento(), evento.contratoId(), evento.correlationId());

        if ("ContratoConcluido".equals(evento.tipoEvento())) {
            reputacaoService.registrarContratoConcluido(
                    evento.eventoId(), evento.contratoId(), evento.freelancerId(), evento.valor());
        } else {
            log.debug("reputacao.evento.ignorado tipoEvento={} contratoId={}",
                    evento.tipoEvento(), evento.contratoId());
        }
    }

    private ContratoEvento desserializar(String mensagem) {
        try {
            return objectMapper.readValue(mensagem, ContratoEvento.class);
        } catch (JsonProcessingException e) {
            log.error("reputacao.evento.desserializacao.falha erro={} mensagem={}",
                    e.getMessage(), mensagem, e);
            return null;
        }
    }
}
