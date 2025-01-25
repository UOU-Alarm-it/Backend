package uou.alarm_it.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uou.alarm_it.domain.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> webCrawling(Integer page);

    void scheduledUpdate();

    Page<Notice> getNoticeList(Integer categoryInt, Integer page);
}
