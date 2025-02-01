package uou.alarm_it.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.notification.dto.NotificationDto;

public interface NotificationService {

    SseEmitter subscribe();

    void sendNotification(NotificationDto notificationDto);
}
