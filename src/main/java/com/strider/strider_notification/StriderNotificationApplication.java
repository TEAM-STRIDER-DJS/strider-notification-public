package com.strider.strider_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"com.strider.strider_notification",
		"com.strider.strider_common_lib"
})
@EnableFeignClients(basePackages = "com.strider.strider_notification.client")
public class StriderNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(StriderNotificationApplication.class, args);
	}

}
