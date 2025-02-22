package uou.alarm_it.notification.contorller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notification.dto.NotificationDto;
import uou.alarm_it.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 25.02.01
     * 작성자 : 류기현
     * SSE 연결 요청
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam(name = "major", defaultValue = "IT융합전공") String major
    ) {
        return notificationService.subscribe(Major.valueOf(major));
    }

    /**
     * 25.02.01
     * 알림 전송 요청 (백엔드 수행 API)
     */
    @PostMapping("/send-notification")
    public void sendNotification(
            @RequestBody NotificationDto notificationDto) {
        notificationService.sendNotification(notificationDto);
    }
}
