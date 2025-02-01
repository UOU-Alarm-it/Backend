package uou.alarm_it.notification.dto;

import lombok.*;
import uou.alarm_it.notice.domain.Notice;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String title;

    private String link;
}
