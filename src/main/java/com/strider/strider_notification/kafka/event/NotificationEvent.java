package com.strider.strider_notification.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.strider.strider_notification.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationEvent(
        NotificationType eventType,
        String receiverUserId,
        String actorUserId,
        String resourceId,
        String content,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {}
