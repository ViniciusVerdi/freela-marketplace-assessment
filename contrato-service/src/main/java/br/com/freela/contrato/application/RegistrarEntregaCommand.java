package br.com.freela.contrato.application;
import java.util.UUID;
public record RegistrarEntregaCommand(UUID contratoId, String correlationId) {}
