package uou.alarm_it.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;
import uou.alarm_it.repository.NoticeRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    // 최근 크롤링한 id 저장
    private static final Set<Long> recentIds = new HashSet<>();
    private static final Integer lastPage = 50;

    // 웹 크롤링
    @Override
    public List<Notice> webCrawling(Integer page) {

        List<Notice> noticeList = new ArrayList<>();

        for (int i = 1; page >= i; i++) {
            String pageStr = Integer.toString(i);
            String IT_URL = "https://ncms.ulsan.ac.kr/cicweb/1024?pageIndex=" + pageStr + "&bbsId=1637&searchCondition=title&searchKeyword=";

            Elements contents;
            try {
                contents = Jsoup.connect(IT_URL).get().select("table.a_brdList tbody");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Element content : contents.select("tr")) {

                long id;
                String title;
                LocalDate date = null;
                String link = null;
                Category category;
                String idString;

                // 공지인 경우, 아닌경우 title, id, category 추출
                if (content.hasClass("noti")) {
                    title = content.select("tr td.bdlTitle b a").text();                                // title
                    idString = content.select("tr td.bdlTitle b a").attr("abs:href");        // id
                    category = Category.NOTICE;                                                                  // category
                } else {
                    title = content.select("td.bdlTitle a").text();
                    idString = content.select("tr td.bdlTitle a").attr("abs:href");
                    category = Category.COMMON;
                }

                id = Long.parseLong(idString.replaceAll(".*no=(\\d+).*", "$1"));                //id

                // date
                try {
                    String dateText = content.select("td.bdlDate").text();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    date = LocalDate.parse(dateText, formatter);
                } catch (DateTimeParseException e) {
                    System.err.println("날짜 형식이 잘못되었습니다: " + e.getMessage());
                }

                try {
                    link = content.select("td.bdlTitle a").attr("abs:href");
                } catch (NullPointerException e) {
                    System.err.println("게시글" + title + "의 링크를 가져오지 못했습니다. " + e.getMessage());

                }

                Notice notice = Notice.builder()
                        .id(id)
                        .title(title)
                        .date(date)
                        .link(link)
                        .category(category)
                        .build();
                noticeList.add(notice);
            }
        }

        return noticeList;
    }

    // 공지 크롤링, 저장 (자동화)
    @Override
    @Scheduled(cron = "0 * * * * *")
    public void scheduledUpdate() {

        List<Notice> notices = webCrawling(1);
        Set<Long> crawlIds = notices.stream().map(Notice::getId).collect(Collectors.toSet());

        Set<Long> willSaveIds = diffWithSaved(crawlIds, recentIds);
        List<Notice> noticesToSave = new ArrayList<>();

        for (Long id : willSaveIds) {
            notices.stream()
                    .filter(notice -> notice.getId().equals(id))
                    .findFirst()
                    .ifPresent(noticesToSave::add);
        }

        noticeRepository.saveAll(noticesToSave);
        log.info("resent post update fin");
        log.info(recentIds.toString());
    }

    // 공지 클롤링, 저장 (전체 페이지)
    @Override
    @Transactional
    public void refresh(Integer page) {
        List<Notice> notices = webCrawling(page);

        noticeRepository.deleteAll();
        noticeRepository.saveAll(notices);
    }

    // 매달 DB 초기화
    @Override
    @Scheduled(cron = "0 0 0 1 * *")
    public void scheduledRefresh() {
        refresh(lastPage);
    }

    // id 의 차이를 구함
    public Set<Long> diffWithSaved(Set<Long> crawlIds, Set<Long> presentIds) {

        Set<Long> wouldSaveIds = new HashSet<>();

        // id를 가지고 있지 않다면, id 를 wouldSaveIds 에 추가
        for (Long id : crawlIds) {
            if (!presentIds.contains(id)) {
                wouldSaveIds.add(id);
            }
        }

        // resentIds hashSet 최신화
        recentIds.clear();
        recentIds.addAll(crawlIds);

        return wouldSaveIds;
    }

    // 공지 조회
    @Override
    public Page<Notice> getNoticeList(Integer categoryInt, Integer page) {

        Category category;

        if (categoryInt == 0) {
            category = Category.NOTICE;
        } else if (categoryInt == 1) {
            category = Category.COMMON;
        } else {
            return noticeRepository.findAllByOrderByIdDesc(PageRequest.of(page, 10, Sort.by("id").descending()));
        }

        return noticeRepository.findAllByCategoryOrderByIdDesc(category, PageRequest.of(page, 10, Sort.by("id").descending()));
    }

    // 검색 기능
    @Override
    public Page<Notice> getNoticeByKeyWord(String keyWord, Integer page) {

        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());

        return noticeRepository.findNoticeByTitleContainingIgnoreCase(keyWord, pageRequest);
    }
}
