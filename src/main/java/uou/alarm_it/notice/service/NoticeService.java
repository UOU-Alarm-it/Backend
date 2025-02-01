package uou.alarm_it.notice.service;

import org.springframework.data.domain.Page;
import uou.alarm_it.notice.domain.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> webCrawling(Integer page);

    void scheduledUpdate();

    void refresh(Integer page);

    void scheduledRefresh();

    Page<Notice> getNoticeList(Integer categoryInt, Integer page);

    Page<Notice> getNoticeByKeyWord(String keyWord, Integer page);
}
