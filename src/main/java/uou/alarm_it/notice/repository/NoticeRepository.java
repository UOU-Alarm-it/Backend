package uou.alarm_it.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notice.domain.Enum.Type;
import uou.alarm_it.notice.domain.Notice;



@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByTypeAndMajor(Type type, Major major, Pageable pageRequest);

    Page<Notice> findAllByMajor(Major major, Pageable pageRequest);

    Page<Notice> findNoticeByTitleContainingIgnoreCaseAndMajor(String title, Major major, Pageable pageReques);
}

