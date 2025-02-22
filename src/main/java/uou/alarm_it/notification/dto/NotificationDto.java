package uou.alarm_it.notification.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String title;

    private String link;

    private String major;
}
