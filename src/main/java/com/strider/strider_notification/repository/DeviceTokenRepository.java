package com.strider.strider_notification.repository;

import com.strider.strider_notification.model.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByUserIdAndActiveTrue(String userId);

    Optional<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByToken(String token);

}