package uou.alarm_it.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;

import java.util.List;


@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByOrderByIdDesc(PageRequest pageRequest);

    Page<Notice> findAllByCategoryOrderByIdDesc(Category category, PageRequest pageRequest);
}
