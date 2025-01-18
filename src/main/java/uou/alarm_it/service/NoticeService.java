package uou.alarm_it.service;

import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> webCrawling(Integer page);

    void scheduledUpdate();

    List<Notice> findByCategory(Integer categoryInt);
}
