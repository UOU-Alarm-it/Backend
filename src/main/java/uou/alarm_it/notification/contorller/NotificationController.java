package uou.alarm_it.notification.contorller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 연결 요청
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }

    // 알림 전송 요청
    @PostMapping("/send-notification")
    public void sendNotification(@RequestBody String notificationDto) {
        notificationService.sendNotification(notificationDto);
    }
}
