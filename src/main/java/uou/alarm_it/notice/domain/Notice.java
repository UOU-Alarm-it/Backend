package uou.alarm_it.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notice.domain.Enum.Type;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notice {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 20, nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String link;

    @Column
    private Type type;

    @Column
    private Major major;
}
