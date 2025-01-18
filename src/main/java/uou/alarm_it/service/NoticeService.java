package uou.alarm_it.service;

import org.springframework.stereotype.Service;
import uou.alarm_it.domain.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> webCrawling(Integer page);
}
