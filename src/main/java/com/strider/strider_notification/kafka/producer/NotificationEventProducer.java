package com.strider.strider_notification.kafka.producer;

import com.strider.strider_notification.kafka.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


/**
 * Kafka 알림 이벤트 Producer (Strider Notification 에서는 사용하지 않음. 타 서비스에서 발행하여 strider-notification이 소비하는 구조)
 *
 * ■ 역할:
 *   NotificationEvent 객체를 Kafka 토픽에 발행
 *   → strider-notification 서비스에서 소비하여 FCM 전송
 *
 * ■ 토픽: notification
 */
@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(NotificationEvent event) {

        kafkaTemplate.send(
                "notification",
                event.receiverUserId(),
                event
        );
    }
}

/*
  ■ 실제 사용 예시

  ■ send() 메서드:
    - KafkaTemplate의 send() 메서드를 호출하여 이벤트 발행
    - 토픽 이름과 이벤트 객체를 인자로 전달
 */

//notificationEventProducer.send(
//    new NotificationEvent(
//                postOwnerId,
//        "새 댓글",
//            "회원님의 게시글에 댓글이 달렸습니다."
//)
//);
