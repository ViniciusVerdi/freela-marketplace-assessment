package br.com.freela.contrato.application;
import java.util.UUID;
public record ConcluirContratoCommand(UUID contratoId, String correlationId) {}
