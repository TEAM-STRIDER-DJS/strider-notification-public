package com.strider.strider_notification.client;

import com.strider.strider_notification.client.dto.CommentThumbnailBatchRequest;
import com.strider.strider_notification.client.dto.CommentThumbnailResponse;
import com.strider.strider_notification.client.dto.Envelope;
import com.strider.strider_notification.client.dto.FeedThumbnailBatchRequest;
import com.strider.strider_notification.client.dto.FeedThumbnailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "feedClient", url = "http://strider-feed:8004")
public interface FeedClient {
    @PostMapping(value = "/api/v1/feed/thumbnails/batch")
    Envelope<List<FeedThumbnailResponse>> getFeedThumbnails(@RequestBody FeedThumbnailBatchRequest request);

    @PostMapping(value = "/api/v1/feed/comment/thumbnails/batch")
    Envelope<List<CommentThumbnailResponse>> getCommentThumbnails(@RequestBody CommentThumbnailBatchRequest request);
}
