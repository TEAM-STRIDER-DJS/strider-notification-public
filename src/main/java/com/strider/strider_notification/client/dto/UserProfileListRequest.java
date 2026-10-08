package com.strider.strider_notification.client.dto;

import java.util.List;

public record UserProfileListRequest(
        List<String> userIds
) {}
