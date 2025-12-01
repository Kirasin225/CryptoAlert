package com.kirasin.CryptoAlert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kirasin.CryptoAlert.entity.Alert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ReactiveRedisTemplate<String, Alert> alertRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper
    ) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisSerializer<Alert> alertSerializer = new RedisSerializer<Alert>() {
            @Override
            public byte[] serialize(Alert alert) throws SerializationException {
                if (alert == null) {
                    return new byte[0];
                }
                try {
                    return objectMapper.writeValueAsBytes(alert);
                } catch (JsonProcessingException e) {
                    throw new SerializationException("Error serializing Alert", e);
                }
            }

            @Override
            public Alert deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(bytes, Alert.class);
                } catch (Exception e) {
                    throw new SerializationException("Error deserializing Alert", e);
                }
            }
        };

        RedisSerializationContext<String, Alert> context = RedisSerializationContext
                .<String, Alert>newSerializationContext(keySerializer)
                .value(alertSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}


