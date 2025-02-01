package uou.alarm_it.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.notice.domain.Enum.Category;
import uou.alarm_it.notice.domain.Notice;



@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByCategory(Category category, Pageable pageRequest);

    Page<Notice> findNoticeByTitleContainingIgnoreCase(String title, Pageable pageRequest);
}

