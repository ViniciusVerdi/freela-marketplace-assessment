package br.com.freela.contrato.infrastructure.web;

import br.com.freela.contrato.application.CancelarContratoCommand;
import br.com.freela.contrato.application.ConcluirContratoCommand;
import br.com.freela.contrato.application.ContratoApplicationService;
import br.com.freela.contrato.application.CriarContratoCommand;
import br.com.freela.contrato.application.RegistrarEntregaCommand;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contratos")
public class ContratoController {

    private static final Logger log = LoggerFactory.getLogger(
            ContratoController.class
    );
    private final ContratoApplicationService service;

    public ContratoController(ContratoApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse criar(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                  @Valid @RequestBody CriarContratoRequest request) {
        log.info("http.contrato.criar correlationId={} clienteId={} freelancerId={} titulo={}", correlationId, request.clienteId(), request.freelancerId(), request.titulo());
        var c = service.criar(new CriarContratoCommand(request.clienteId(), request.freelancerId(), request.titulo(), request.valor(), correlationId));
        log.info("http.contrato.criar.response correlationId={} contratoId={} status={}", correlationId, c.id(), c.status());
        return ContratoResponse.from(c);
    }

    @GetMapping("/{id}")
    public ContratoResponse buscar(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,  @PathVariable("id") UUID id) {
        log.info("http.contrato.buscar correlationId={} contratoId={}", correlationId, id);
        return ContratoResponse.from(service.buscar(id));
    }

    @GetMapping
    public List<ContratoResponse> listar(@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        log.info("http.contrato.listar correlationId={}", correlationId);
        return service.listar().stream().map(ContratoResponse::from).toList();
    }

    @PostMapping("/entrega/{id}")
    public ContratoResponse registrarEntrega(
            @RequestHeader(value = "X-Correlation-Id", required = false)
            String correlationId,
            @PathVariable("id") UUID id
    ) {
        log.info("http.contrato.registrarEntrega correlationId={} contratoId={}", correlationId,id
        );
        var c = service.registrarEntrega(new RegistrarEntregaCommand(id, correlationId));
        log.info(
                "http.contrato.registrarEntrega.response correlationId={} contratoId={} status={}",
                correlationId,
                c.id(),
                c.status()
        );
        return ContratoResponse.from(c);
    }

    @PostMapping("/concluir/{id}")
    public ContratoResponse concluir(
            @RequestHeader(value = "X-Correlation-Id", required = false)
            String correlationId,
            @PathVariable("id") UUID id
    ) {
        log.info("http.contrato.concluir correlationId={} contratoId={}",correlationId,id
        );
        var c = service.concluir(new ConcluirContratoCommand(id, correlationId));
        log.info(
                "http.contrato.concluir.response correlationId={} contratoId={} status={}",
                correlationId,
                c.id(),
                c.status()
        );
        return ContratoResponse.from(c);
    }

    @PostMapping("/cancelar/{id}")
    public ContratoResponse cancelar(
            @RequestHeader(value = "X-Correlation-Id", required = false)
            String correlationId,
            @PathVariable("id") UUID id
    ) {
        log.info("http.contrato.cancelar correlationId={} contratoId={}", correlationId, id);
        var c = service.cancelar(new CancelarContratoCommand(id, correlationId));
        log.info(
                "http.contrato.cancelar.response correlationId={} contratoId={} status={}",
                correlationId,
                c.id(),
                c.status()
        );
        return ContratoResponse.from(c);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
