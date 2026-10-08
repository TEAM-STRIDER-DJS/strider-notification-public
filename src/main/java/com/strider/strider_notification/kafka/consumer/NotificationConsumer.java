package com.strider.strider_notification.kafka.consumer;

import com.strider.strider_notification.kafka.event.NotificationEvent;
import com.strider.strider_notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "notification",
            groupId = "strider-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(NotificationEvent event, Acknowledgment acknowledgment) {
        log.info("event received = {}", event);

        try {
            notificationService.process(event);

            acknowledgment.acknowledge();
            log.info("notification consume succeed - {}", event.content());
        } catch (Exception e) {
            log.error("notification consume fail", e);
            /*
             * ack 하지 않음
             * -> 재처리 가능
             */
        }
    }
}
