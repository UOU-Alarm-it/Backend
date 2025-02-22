package uou.alarm_it.notice.service;

import org.springframework.data.domain.Page;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notice.domain.Notice;

import java.util.List;
import java.util.Set;

public interface NoticeService {

    List<Notice> webCrawling(Integer page, Major major);

    void crawlAndSave(Integer page, Major major);

    void refresh(Integer page);

    Page<Notice> getNoticeList(Integer categoryInt, Integer page);

    Page<Notice> getNoticeByKeyWord(String keyWord, Integer page);
}
