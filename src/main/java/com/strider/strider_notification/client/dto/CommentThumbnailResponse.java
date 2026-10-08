package com.strider.strider_notification.client.dto;

public record CommentThumbnailResponse(
        String commentId,
        String feedPostId,
        String thumbnailUrl
) {}
