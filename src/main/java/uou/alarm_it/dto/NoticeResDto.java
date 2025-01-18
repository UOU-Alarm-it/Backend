package uou.alarm_it.dto;

import lombok.Getter;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;

import java.time.LocalDate;

public class NoticeResDto {

    @Getter
    public static class ViewResDto {
        private final Long id;
        private final String title;
        private final LocalDate date;
        private final Category category;
        private final String link;

        public ViewResDto(Notice notice) {
            this.id = notice.getId();
            this.title = notice.getTitle();
            this.date = notice.getDate();
            this.category = notice.getCategory();
            this.link = notice.getLink();
        }
    }
}
