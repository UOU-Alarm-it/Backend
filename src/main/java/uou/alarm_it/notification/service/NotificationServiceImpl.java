package uou.alarm_it.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.notification.dto.NotificationDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationServiceImpl implements NotificationService {

    // 클라이언트 등록
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // SSE 연결을 생성하고 클라이언트 등록 (알림)
    @Override
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);    // 타임 아웃 무한대 설정
        emitters.add(emitter);

        // 연결이 종료되면 클라이언트 제거
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    // 알림 전송
    @Override
    public void sendNotification(NotificationDto notificationDto) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")   // 이벤트 이름
                        .data(notificationDto));    // 전송할 데이터
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        // 연결이 끊긴 클라이언트 제거
        emitters.removeAll(deadEmitters);
    }
}
