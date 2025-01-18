package uou.alarm_it.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;

import java.util.List;


@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByIdDesc();

    List<Notice> findAllByCategoryOrderByIdDesc(Category category);
}
