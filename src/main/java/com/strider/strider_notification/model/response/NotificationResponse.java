package com.strider.strider_notification.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.strider.strider_notification.model.NotificationType;
import com.strider.strider_notification.model.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        NotificationType eventType,
        String actorUserId,
        String resourceId,
        String content,
        boolean isRead,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        String actorProfileImageUrl,
        String resourceThumbnailUrl,
        String targetFeedPostId
) {
    public static NotificationResponse from(Notification notification) {
        return from(notification, null, null, null);
    }

    public static NotificationResponse from(
            Notification notification,
            String actorProfileImageUrl,
            String resourceThumbnailUrl,
            String targetFeedPostId
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventType(),
                notification.getActorUserId(),
                notification.getResourceId(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt(),
                actorProfileImageUrl,
                resourceThumbnailUrl,
                targetFeedPostId
        );
    }
}
