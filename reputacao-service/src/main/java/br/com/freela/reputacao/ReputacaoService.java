package br.com.freela.reputacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ReputacaoService {

    private static final Logger log = LoggerFactory.getLogger(ReputacaoService.class);
    private final ReputacaoRepository repository;
    private final EventoProcessadoRepository eventoProcessadoRepository;

    public ReputacaoService(ReputacaoRepository repository, EventoProcessadoRepository eventoProcessadoRepository) {
        this.repository = repository;
        this.eventoProcessadoRepository = eventoProcessadoRepository;
    }

    @Transactional
    public void registrarContratoConcluido(UUID eventoId, UUID contratoId, UUID freelancerId, BigDecimal valor) {
        if (eventoProcessadoRepository.existsById(eventoId)) {
            log.info("reputacao.duplicado.ignorando eventoId={} contratoId={}", eventoId, contratoId);
            return;
        }
        eventoProcessadoRepository.save(new EventoProcessado(eventoId));
        log.info("reputacao.atualizacao.inicio contratoId={} freelancerId={} valor={}", contratoId, freelancerId, valor);
        var r = repository.findById(freelancerId).orElseGet(() -> new ReputacaoFreelancer(freelancerId));
        r.registrarContrato(valor);
        repository.save(r);
        log.info("reputacao.atualizacao.sucesso contratoId={} freelancerId={} contratosConcluidos={} valorTotal={}",
                contratoId, freelancerId, r.contratosConcluidos, r.valorTotal);
    }
}
