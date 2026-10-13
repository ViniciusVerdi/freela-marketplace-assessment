package br.com.freela.contrato.domain.shared;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {
    private final List<DomainEvent> eventos = new ArrayList<>();

    protected void registrar(DomainEvent domainEvent){
        eventos.add(domainEvent);
    }
    public List<DomainEvent> getEventos(){
        return List.copyOf(eventos);
    }

    public void limpar(){
        eventos.clear();
    }
}
