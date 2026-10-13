package br.com.freela.contrato.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic contratoEventos(){
        return TopicBuilder.name("contrato.events").partitions(3).replicas(1).build();
    }
}
