package com.strider.strider_notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.strider.strider_notification.exception.InvalidFcmTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseApp firebaseApp;

    public void send(
            String token,
            String title,
            String body
    ) {

        try {

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .build();

            FirebaseMessaging.getInstance(firebaseApp)
                    .send(message);

        } catch (FirebaseMessagingException e) {

            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            if (errorCode == MessagingErrorCode.UNREGISTERED
                    || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                throw new InvalidFcmTokenException(token);
            }

            log.error("fcm send fail token={} errorCode={}", token, errorCode, e);
            throw new RuntimeException(e);
        }
    }
}
