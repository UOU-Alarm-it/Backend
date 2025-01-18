package uou.alarm_it.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import uou.alarm_it.domain.Enum.Category;

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
    private Category category;
}
