package uou.alarm_it.notice.dto;

import lombok.Getter;
import uou.alarm_it.notice.domain.Enum.Type;
import uou.alarm_it.notice.domain.Notice;

import java.time.LocalDate;

public class NoticeResDto {

    @Getter
    public static class ViewResDto {
        private final Long id;
        private final String title;
        private final LocalDate date;
        private final Type type;
        private final String link;

        public ViewResDto(Notice notice) {
            this.id = notice.getId();
            this.title = notice.getTitle();
            this.date = notice.getDate();
            this.type = notice.getType();
            this.link = notice.getLink();
        }
    }
}
