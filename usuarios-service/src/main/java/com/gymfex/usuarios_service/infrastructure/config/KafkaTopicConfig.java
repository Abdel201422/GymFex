package com.gymfex.usuarios_service.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic socioCreatedTopic() {
        return TopicBuilder
                .name("usuarios.socio.created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic socioUpdatedTopic() {
        return TopicBuilder
                .name("usuarios.socio.updated")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic adminCreatedTopic() {
        return TopicBuilder
                .name("usuarios.admin.created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic adminUpdatedTopic() {
        return TopicBuilder
                .name("usuarios.admin.updated")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic usuarioDeletedTopic() {
        return TopicBuilder
                .name("usuarios.usuario.deleted")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
