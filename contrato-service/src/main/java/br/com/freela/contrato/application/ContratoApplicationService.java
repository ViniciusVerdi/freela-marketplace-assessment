package br.com.freela.contrato.application;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.repository.ContratoRepository;
import br.com.freela.contrato.domain.shared.DomainEvent;
import br.com.freela.contrato.infrastructure.kafka.ProdutorEventosContrato;
import br.com.freela.contrato.infrastructure.persistence.EventoOutbox;
import br.com.freela.contrato.infrastructure.persistence.EventoOutboxRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoApplicationService {

    private static final Logger log = LoggerFactory.getLogger(
        ContratoApplicationService.class
    );
    private final ContratoRepository repository;
    private final ProdutorEventosContrato produtorEventos;
    private final EventoOutboxRepository outboxRepository;

    public ContratoApplicationService(
        ContratoRepository repository,
        ProdutorEventosContrato produtorEventos,
        EventoOutboxRepository outboxRepository
    ) {
        this.repository = repository;
        this.produtorEventos = produtorEventos;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public Contrato criar(CriarContratoCommand cmd) {
        log.info(
            "contrato.criacao.inicio clienteId={} freelancerId={} titulo={} valor={}",
            cmd.clienteId(),
            cmd.freelancerId(),
            cmd.titulo(),
            cmd.valor()
        );
        Contrato contrato = Contrato.criar(
            cmd.clienteId(),
            cmd.freelancerId(),
            cmd.titulo(),
            cmd.valor(),
            cmd.correlationId()
        );
        log.info(
            "contrato.dominio.criado contratoId={} status={} eventos={}",
            contrato.id(),
            contrato.status(),
            contrato.eventos().size()
        );
        Contrato salvo = repository.salvar(contrato);

        processarEventosDeDominio(contrato);

        log.info(
            "contrato.criacao.sucesso contratoId={} clienteId={} freelancerId={} status={}",
            salvo.id(),
            salvo.clienteId(),
            salvo.freelancerId(),
            salvo.status()
        );
        return salvo;
    }

    @Transactional
    public Contrato registrarEntrega(RegistrarEntregaCommand cmd) {
        log.info(
            "contrato.entrega.inicio contratoId={} correlationId={}",
            cmd.contratoId(),
            cmd.correlationId()
        );
        Contrato contrato = repository
            .buscarPorId(cmd.contratoId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Contrato não encontrado: " + cmd.contratoId()
                )
            );
        contrato.registrarEntrega(cmd.correlationId());

        Contrato salvo = repository.salvar(contrato);

        processarEventosDeDominio(contrato);
        log.info(
            "contrato.entrega.sucesso contratoId={} status={}",
            salvo.id(),
            salvo.status()
        );
        return salvo;
    }

    @Transactional
    public Contrato concluir(ConcluirContratoCommand cmd) {
        log.info(
            "contrato.conclusao.inicio contratoId={} correlationId={}",
            cmd.contratoId(),
            cmd.correlationId()
        );
        Contrato contrato = repository
            .buscarPorId(cmd.contratoId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Contrato não encontrado: " + cmd.contratoId()
                )
            );
        contrato.concluir(cmd.correlationId());
        Contrato salvo = repository.salvar(contrato);
        processarEventosDeDominio(contrato);
        log.info(
            "contrato.conclusao.sucesso contratoId={} status={}",
            salvo.id(),
            salvo.status()
        );
        return salvo;
    }

    @Transactional
    public Contrato cancelar(CancelarContratoCommand cmd) {
        log.info(
            "contrato.cancelamento.inicio contratoId={} correlationId={}",
            cmd.contratoId(),
            cmd.correlationId()
        );
        Contrato contrato = repository
            .buscarPorId(cmd.contratoId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Contrato não encontrado: " + cmd.contratoId()
                )
            );
        contrato.cancelar(cmd.correlationId());
        Contrato salvo = repository.salvar(contrato);
        processarEventosDeDominio(contrato);
        log.info(
            "contrato.cancelamento.sucesso contratoId={} status={}",
            salvo.id(),
            salvo.status()
        );
        return salvo;
    }

    @Transactional(readOnly = true)
    public Contrato buscar(UUID id) {
        log.info("contrato.busca.inicio contratoId={}", id);
        var contrato = repository
            .buscarPorId(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Contrato não encontrado: " + id)
            );
        log.info(
            "contrato.busca.sucesso contratoId={} status={}",
            id,
            contrato.status()
        );
        return contrato;
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        log.info("contrato.listagem.inicio");
        var contratos = repository.listar();
        log.info("contrato.listagem.sucesso quantidade={}", contratos.size());
        return contratos;
    }

    private void processarEventosDeDominio(Contrato contrato) {
        for (DomainEvent evento : contrato.pullDomainEvents()) {
            String payload = produtorEventos.serializar(evento);
            outboxRepository.save(new EventoOutbox(
                    evento.eventoId(),
                    evento.tipoEvento(),
                    evento.correlationId(),
                    evento.contratoId(),
                    payload
            ));
            log.info(
                "contrato.outbox.registrado contratoId={} eventoId={} tipoEvento={}",
                contrato.id(),
                evento.eventoId(),
                evento.tipoEvento()
            );
        }
    }
}
