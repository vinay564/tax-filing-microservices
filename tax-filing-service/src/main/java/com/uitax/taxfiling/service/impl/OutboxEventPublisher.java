package com.uitax.taxfiling.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.uitax.taxfiling.entity.OutboxEvent;
import com.uitax.taxfiling.repository.OutboxEventRepository;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final String TOPIC = "tax-filing-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                 KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishUnpublishedEvents() {

        List<OutboxEvent> unpublished = outboxEventRepository.findByPublishedFalse();

        if (unpublished.isEmpty()) {
            return;
        }

        log.info("Found {} unpublished outbox event(s)", unpublished.size());

        for (OutboxEvent event : unpublished) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload()).get();

                event.markPublished();
                outboxEventRepository.save(event);

                log.info("Published outbox event {} to topic {}", event.getId(), TOPIC);

            } catch (Exception e) {
                log.error("Failed to publish outbox event {}. Will retry on next cycle.", event.getId(), e);
            }
        }
    }

}