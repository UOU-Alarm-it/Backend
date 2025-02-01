package uou.alarm_it.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationDto {

    private String title;

    public NotificationDto(String title) {
        this.title = title;
    }

}
