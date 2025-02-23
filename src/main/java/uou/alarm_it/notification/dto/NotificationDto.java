package uou.alarm_it.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    @NotBlank(message = "공지 제목을 작성해주세요.")
    private String title;

    private String link;

    private String major;
}
