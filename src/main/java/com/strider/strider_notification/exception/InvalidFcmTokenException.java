package com.strider.strider_notification.exception;

public class InvalidFcmTokenException extends RuntimeException {

    public InvalidFcmTokenException(String token) {
        super("Invalid FCM token: " + token);
    }
}
