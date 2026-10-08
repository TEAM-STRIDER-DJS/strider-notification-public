package com.strider.strider_notification.client.dto;

public record Envelope<T>(
        T data,
        Meta meta
) {
    public record Meta(
            String status,
            Integer code,
            String message
    ) {}
}
