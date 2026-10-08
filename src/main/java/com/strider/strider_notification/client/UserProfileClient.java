package com.strider.strider_notification.client;

import com.strider.strider_notification.client.dto.Envelope;
import com.strider.strider_notification.client.dto.SimpleUserProfileResponse;
import com.strider.strider_notification.client.dto.UserProfileListRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "userProfileClient", url = "http://strider-user-profile:8001")
public interface UserProfileClient {
    @PostMapping(value = "/api/v1/user/profile/find/batch")
    Envelope<List<SimpleUserProfileResponse>> getSimpleUserProfileList(@RequestBody UserProfileListRequest request);
}
