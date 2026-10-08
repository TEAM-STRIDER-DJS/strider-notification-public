package com.strider.strider_notification.client.dto;

public record SimpleUserProfileResponse(
        String userId,
        String profileId,
        String nickname,
        String profileImage,
        String profileThumbImage,
        String rankId
) {}
