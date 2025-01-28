package uou.alarm_it.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;



@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByOrderByIdDesc(Pageable pageRequest);

    Page<Notice> findAllByCategoryOrderByIdDesc(Category category, Pageable pageRequest);

    Page<Notice> findNoticeByTitleContainingIgnoreCase(String title, Pageable pageRequest);
}

