package com.strider.strider_notification.controller;

import com.strider.strider_common_lib.response.StriderResponse;
import com.strider.strider_common_lib.utils.TokenUtils;
import com.strider.strider_notification.model.request.DeviceTokenRegisterRequest;
import com.strider.strider_notification.model.response.NotificationResponse;
import com.strider.strider_notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/noti")
public class NotificationRestController {

    private final NotificationService notificationService;
    private final TokenUtils tokenUtils;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestHeader HttpHeaders headers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = tokenUtils.getUidFrom(headers);
        Slice<NotificationResponse> result = notificationService.getNotifications(userId, page, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.success(result));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id
    ) {
        String userId = tokenUtils.getUidFrom(headers);
        notificationService.markAsRead(userId, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @RequestHeader HttpHeaders headers,
            @PathVariable String id
    ) {
        String userId = tokenUtils.getUidFrom(headers);
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.success(null));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @RequestHeader HttpHeaders headers
    ) {
        String userId = tokenUtils.getUidFrom(headers);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.success(count));
    }

    @PostMapping("/device-token")
    public ResponseEntity<?> registerDeviceToken(
            @RequestHeader HttpHeaders headers,
            @RequestBody DeviceTokenRegisterRequest request
    ) {
        String userId = tokenUtils.getUidFrom(headers);
        notificationService.registerDeviceToken(userId, request.token());
        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.success(null));
    }
}
