package uou.alarm_it.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notification.dto.NotificationDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    // Major별 클라이언트 등록
    private final Map<Major, List<SseEmitter>> emittersByMajor = new ConcurrentHashMap<>();

    // SSE 연결을 생성하고 클라이언트 등록 (알림)
    @Override
    public SseEmitter subscribe(Major major) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);  // 타임 아웃 무한대 설정
        emittersByMajor.computeIfAbsent(major, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 연결이 종료되면 클라이언트 제거
        emitter.onCompletion(() -> removeEmitter(major, emitter));
        emitter.onTimeout(() -> removeEmitter(major, emitter));
        emitter.onError((e) -> removeEmitter(major, emitter));

        return emitter;
    }

    // 특정 Major에게 알림 전송
    @Override
    public void sendNotification(NotificationDto notificationDto) {

        Major major;

        try {
            major = Major.valueOf(notificationDto.getMajor());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid major");
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Null major");
        }

        List<SseEmitter> emitters = emittersByMajor.getOrDefault(major, Collections.emptyList());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")   // 이벤트 이름
                        .data(notificationDto)); // 전송할 데이터
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        // 연결이 끊긴 클라이언트 제거
        emitters.removeAll(deadEmitters);
        log.info("알람을 받은 {} 클라이언트(이용자) 수 : {}", major, emitters.size());
    }

    // 특정 Major에서 Emitter 제거
    private void removeEmitter(Major major, SseEmitter emitter) {
        emittersByMajor.getOrDefault(major, Collections.emptyList()).remove(emitter);
    }
}