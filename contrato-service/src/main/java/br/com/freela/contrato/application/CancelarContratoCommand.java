package br.com.freela.contrato.application;
import java.util.UUID;
public record CancelarContratoCommand(UUID contratoId, String correlationId) {}
