package com.strider.strider_notification.client.dto;

import java.util.List;

public record FeedThumbnailBatchRequest(
        List<String> feedPostIds
) {}
