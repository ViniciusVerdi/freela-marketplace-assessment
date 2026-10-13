package br.com.freela.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private final NotificacaoRepository repository;
    private final EventoProcessadoRepository eventoProcessadoRepository;

    public NotificacaoService(NotificacaoRepository repository, EventoProcessadoRepository eventoProcessadoRepository) {
        this.repository = repository;
        this.eventoProcessadoRepository = eventoProcessadoRepository;
    }

    @Transactional
    public void registrar(UUID eventoId, UUID contratoId, UUID destinatarioId, String tipo, String mensagem) {
        if (eventoProcessadoRepository.existsById(eventoId)) {
            log.info("notificacao.duplicado.ignorando eventoId={} contratoId={}", eventoId, contratoId);
            return;
        }
        eventoProcessadoRepository.save(new EventoProcessado(eventoId));
        log.info("notificacao.registro.inicio contratoId={} destinatarioId={} tipo={}", contratoId, destinatarioId, tipo);
        var n = repository.save(new Notificacao(contratoId, destinatarioId, tipo, mensagem));
        log.info("notificacao.registro.sucesso notificacaoId={} contratoId={} destinatarioId={}", n.id, contratoId, destinatarioId);
    }
}
