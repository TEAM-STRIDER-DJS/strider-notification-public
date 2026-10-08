package com.strider.strider_notification.repository;

import com.strider.strider_notification.model.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Slice<Notification> findAllByReceiverUserIdOrderByCreatedAtDesc(String receiverUserId, Pageable pageable);

    long countByReceiverUserIdAndIsReadFalse(String receiverUserId);
}
