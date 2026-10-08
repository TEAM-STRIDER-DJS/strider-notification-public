package com.strider.strider_notification.service;

import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;
import com.strider.strider_notification.client.FeedClient;
import com.strider.strider_notification.client.UserProfileClient;
import com.strider.strider_notification.client.dto.CommentThumbnailBatchRequest;
import com.strider.strider_notification.client.dto.CommentThumbnailResponse;
import com.strider.strider_notification.client.dto.Envelope;
import com.strider.strider_notification.client.dto.FeedThumbnailBatchRequest;
import com.strider.strider_notification.client.dto.FeedThumbnailResponse;
import com.strider.strider_notification.client.dto.SimpleUserProfileResponse;
import com.strider.strider_notification.client.dto.UserProfileListRequest;
import com.strider.strider_notification.exception.InvalidFcmTokenException;
import com.strider.strider_notification.kafka.event.NotificationEvent;
import com.strider.strider_notification.model.NotificationType;
import com.strider.strider_notification.model.entity.DeviceToken;
import com.strider.strider_notification.model.entity.Notification;
import com.strider.strider_notification.model.response.NotificationResponse;
import com.strider.strider_notification.repository.DeviceTokenRepository;
import com.strider.strider_notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private static final Set<NotificationType> FEED_POST_RESOURCE_TYPES =
            EnumSet.of(NotificationType.LIKE, NotificationType.COMMENT, NotificationType.POST);

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;
    private final UserProfileClient userProfileClient;
    private final FeedClient feedClient;

    public void process(NotificationEvent event) {

        /*
         * 1. Notification 저장
         */
        Notification notification = Notification.builder()
                .receiverUserId(event.receiverUserId())
                .actorUserId(event.actorUserId())
                .eventType(event.eventType())
                .content(event.content())
                .resourceId(event.resourceId())
                .isRead(false)
                .createdAt(event.createdAt())
                .build();

        notificationRepository.save(notification);

        /*
         * 2. DeviceToken 조회
         */
        List<DeviceToken> deviceTokens =
                deviceTokenRepository.findAllByUserIdAndActiveTrue(
                        event.receiverUserId()
                );

        if (deviceTokens.isEmpty()) {

            log.info(
                    "device token not found userId={}",
                    event.receiverUserId()
            );

            return;
        }

        /*
         * 3. FCM 발송
         */
        for (DeviceToken deviceToken : deviceTokens) {

            try {

                fcmService.send(
                        deviceToken.getToken(),
                        getTitle(event.eventType()),
                        event.content()
                );
                log.info("notification sent : {}", event.content());

            } catch (InvalidFcmTokenException e) {

                log.warn("deactivating invalid fcm token={}", deviceToken.getToken());
                deviceToken.deactivate();

            } catch (Exception e) {

                log.error("fcm send fail token={}", deviceToken.getToken(), e);
            }
        }
    }

    public Slice<NotificationResponse> getNotifications(String userId, int page, int size) {

        Slice<Notification> notifications = notificationRepository
                .findAllByReceiverUserIdOrderByCreatedAtDesc(
                        userId,
                        PageRequest.of(page, size)
                );

        List<Notification> content = notifications.getContent();
        if (content.isEmpty()) {
            return notifications.map(NotificationResponse::from);
        }

        Map<String, String> profileImageByActorId = fetchProfileImages(content);
        FeedLinkData feedLinkData = fetchFeedLinkData(content);

        return notifications.map(notification -> NotificationResponse.from(
                notification,
                profileImageByActorId.get(notification.getActorUserId()),
                feedLinkData.thumbnailByResourceId().get(notification.getResourceId()),
                feedLinkData.feedPostIdByResourceId().get(notification.getResourceId())
        ));
    }

    private record FeedLinkData(
            Map<String, String> thumbnailByResourceId,
            Map<String, String> feedPostIdByResourceId
    ) {}

    private Map<String, String> fetchProfileImages(List<Notification> notifications) {

        List<String> actorUserIds = notifications.stream()
                .map(Notification::getActorUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (actorUserIds.isEmpty()) {
            return Map.of();
        }

        try {
            Envelope<List<SimpleUserProfileResponse>> envelope =
                    userProfileClient.getSimpleUserProfileList(new UserProfileListRequest(actorUserIds));

            if (envelope == null || envelope.data() == null) {
                return Map.of();
            }

            Map<String, String> result = new HashMap<>();
            for (SimpleUserProfileResponse profile : envelope.data()) {
                String imageUrl = profile.profileThumbImage() != null
                        ? profile.profileThumbImage()
                        : profile.profileImage();
                result.put(profile.userId(), imageUrl);
            }
            return result;

        } catch (Exception e) {
            log.warn("failed to fetch actor profile images", e);
            return Map.of();
        }
    }

    private FeedLinkData fetchFeedLinkData(List<Notification> notifications) {

        List<String> feedPostIds = notifications.stream()
                .filter(n -> FEED_POST_RESOURCE_TYPES.contains(n.getEventType()))
                .map(Notification::getResourceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> commentIds = notifications.stream()
                .filter(n -> n.getEventType() == NotificationType.COMMENT_LIKE)
                .map(Notification::getResourceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, String> thumbnailByResourceId = new HashMap<>();
        Map<String, String> feedPostIdByResourceId = new HashMap<>();

        if (!feedPostIds.isEmpty()) {
            try {
                Envelope<List<FeedThumbnailResponse>> envelope =
                        feedClient.getFeedThumbnails(new FeedThumbnailBatchRequest(feedPostIds));

                if (envelope != null && envelope.data() != null) {
                    for (FeedThumbnailResponse thumbnail : envelope.data()) {
                        thumbnailByResourceId.put(thumbnail.feedPostId(), thumbnail.thumbnailUrl());
                        // LIKE/COMMENT/POST는 resourceId 자체가 이미 feedPostId
                        feedPostIdByResourceId.put(thumbnail.feedPostId(), thumbnail.feedPostId());
                    }
                }
            } catch (Exception e) {
                log.warn("failed to fetch feed thumbnails", e);
            }
        }

        if (!commentIds.isEmpty()) {
            try {
                Envelope<List<CommentThumbnailResponse>> envelope =
                        feedClient.getCommentThumbnails(new CommentThumbnailBatchRequest(commentIds));

                if (envelope != null && envelope.data() != null) {
                    for (CommentThumbnailResponse thumbnail : envelope.data()) {
                        thumbnailByResourceId.put(thumbnail.commentId(), thumbnail.thumbnailUrl());
                        // COMMENT_LIKE는 resourceId(commentId) → 게시물로 역참조한 feedPostId
                        feedPostIdByResourceId.put(thumbnail.commentId(), thumbnail.feedPostId());
                    }
                }
            } catch (Exception e) {
                log.warn("failed to fetch comment thumbnails", e);
            }
        }

        return new FeedLinkData(thumbnailByResourceId, feedPostIdByResourceId);
    }

    public void markAsRead(String userId, String notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        if (!notification.getReceiverUserId().equals(userId)) {
            throw new StriderException(StriderErrorCodes.FORBIDDEN);
        }

        notification.markAsRead();
    }

    // 알림 단건 삭제 (본인 알림만)
    public void deleteNotification(String userId, String notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new StriderException(StriderErrorCodes.NOT_FOUND));

        if (!notification.getReceiverUserId().equals(userId)) {
            throw new StriderException(StriderErrorCodes.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }

    public long getUnreadCount(String userId) {

        return notificationRepository.countByReceiverUserIdAndIsReadFalse(userId);
    }

    public void registerDeviceToken(String userId, String token) {

        // token은 컬럼 unique 제약이 있어, 같은 토큰(=같은 디바이스)이 다른 유저 소유로 남아있으면
        // 그 소유권을 정리하지 않고는 INSERT/UPDATE 시 unique violation이 발생한다.
        deviceTokenRepository.findByToken(token)
                .filter(deviceToken -> !deviceToken.getUserId().equals(userId))
                .ifPresent(deviceTokenRepository::delete);

        deviceTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        deviceToken -> deviceToken.updateToken(token),
                        () -> deviceTokenRepository.save(
                                DeviceToken.builder()
                                        .userId(userId)
                                        .token(token)
                                        .active(true)
                                        .build()
                        )
                );
    }

    private String getTitle(NotificationType type) {

        return switch (type) {

            case COMMENT -> "새 댓글";
            case FOLLOW -> "새 팔로우";
            case LIKE -> "새 좋아요";
            case COMMENT_LIKE -> "새 댓글 좋아요";
            case POST -> "새 게시물";
            case FOLLOW_REQUEST -> "팔로우 요청";
            case FOLLOW_ACCEPTED -> "팔로우 수락";
            default -> "알림";
        };
    }
}
