package br.com.freela.auditoria;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface EventoAuditoriaRepository
    extends JpaRepository<EventoAuditoria, UUID>
{
    boolean existsByEventId(UUID eventId);
}
