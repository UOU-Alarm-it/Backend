package uou.alarm_it.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uou.alarm_it.domain.Notice;


@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

}
