package uou.alarm_it.notice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notification.dto.NotificationDto;
import uou.alarm_it.notification.service.NotificationService;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduledTask {

    private final NotificationService notificationService;
    private final NoticeService noticeService;

    private final Integer maxPage = 10;
    private final NoticeServiceImpl noticeServiceImpl;

    // 공지 크롤링, 저장 (자동화)
    @Scheduled(cron = "0 * * * * *")
    public synchronized void scheduledUpdate() {

        // 알람 테스트 코드 - 시작
        notificationService.sendNotification(
                NotificationDto.builder()
                        .title("IT융합전공 알람 TEST")
                        .link("https://ncms.ulsan.ac.kr/cicweb/1024?action=view&no=255917")
                        .major("IT융합전공")
                        .build());

        notificationService.sendNotification(
                NotificationDto.builder()
                        .title("AI융합전공 알람 TEST")
                        .link("https://ai.ulsan.ac.kr/ai/1105?action=view&no=261956")
                        .major("AI융합전공")
                        .build());

        notificationService.sendNotification(
                NotificationDto.builder()
                        .title("ICT융합학부 알람 TEST")
                        .link("https://ict.ulsan.ac.kr/ict/5786?action=view&no=259308")
                        .major("ICT융합학부")
                        .build());

        for (Major major : Major.values()) {
            noticeService.crawlAndSave(1, major);
        }

        NoticeServiceImpl.NOTIFICATION = true;

    }

    // 매달 DB 초기화
    @Scheduled(cron = "0 0 0 1 * *")
    public synchronized void scheduledRefresh() {
        noticeService.refresh(maxPage);
    }
}
