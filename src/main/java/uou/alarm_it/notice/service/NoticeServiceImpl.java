package uou.alarm_it.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uou.alarm_it.notice.domain.Enum.Category;
import uou.alarm_it.notice.domain.Notice;
import uou.alarm_it.notice.repository.NoticeRepository;
import uou.alarm_it.notification.dto.NotificationDto;
import uou.alarm_it.notification.service.NotificationService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    private static final Set<Long> recentIds = new HashSet<>(); // 최근 크롤링한 ID
    private static final Integer lastPage = 50;
    private final NotificationService notificationService;

    private static final String BASE_URL = "https://ncms.ulsan.ac.kr/cicweb/1024";
    private static final boolean USE_LOCAL_HTML = false; // true: 로컬 HTML 테스트 모드, false: 웹 크롤링 모드
    private static boolean NOTIFICATION = false;

    private Integer j = 0;

    // 웹 크롤링
    @Override
    public List<Notice> webCrawling(Integer page) {

        List<Notice> noticeList = new ArrayList<>();


        for (int i = 1; page >= i; i++) {
            String pageStr = Integer.toString(i);
            String IT_URL = BASE_URL + "?pageIndex=" + pageStr + "&bbsId=1637&searchCondition=title&searchKeyword=";

            Elements contents;

            if (USE_LOCAL_HTML) {
                // 로컬 HTML 파일을 이용한 테스트 모드
                ClassPathResource resource = new ClassPathResource("html/test" + j + ".html");
                j++;

                if (!resource.exists()) {
                    throw new RuntimeException("HTML 파일을 찾을 수 없습니다.");
                }

                try {
                    Document doc = Jsoup.parse(resource.getFile(), StandardCharsets.UTF_8.name());
                    contents = doc.select("table.a_brdList tbody");
                } catch (IOException e) {
                    throw new RuntimeException("HTML 파일을 파싱하는 중 오류 발생: " + e.getMessage(), e);
                }
            } else {
                try {
                    contents = Jsoup.connect(IT_URL).get().select("table.a_brdList tbody");
                } catch (IOException e) {
                    throw new RuntimeException("페이지 로드 실패: " + IT_URL, e);
                }
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
                    idString = content.select("tr td.bdlTitle b a").attr("href");        // id
                    category = Category.NOTICE;                                                                  // category
                } else {
                    title = content.select("td.bdlTitle a").text();
                    idString = content.select("tr td.bdlTitle a").attr("href");
                    category = Category.COMMON;
                }

                if (idString == null || idString.isEmpty()) {
                    throw new IllegalArgumentException("idString이 null이거나 비어 있습니다." + title);
                }

                Pattern pattern = Pattern.compile("no=(\\d+)");
                Matcher matcher = pattern.matcher(idString);

                if (matcher.find()) {
                    id = Long.parseLong(matcher.group(1));
                } else {
                    throw new NumberFormatException("ID를 찾을 수 없습니다. URL: " + idString);
                }

                // date
                try {
                    String dateText = content.select("td.bdlDate").text();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    date = LocalDate.parse(dateText, formatter);
                } catch (DateTimeParseException e) {
                    System.err.println("날짜 형식이 잘못되었습니다: " + e.getMessage());
                }

                try {
                    link = content.select("td.bdlTitle a").attr("href");
                    if (link.isEmpty()) {
                        System.err.println("게시글 '" + title + "'의 링크를 찾을 수 없습니다.");
                    }
                    link = BASE_URL + link;
                } catch (Exception e) {
                    System.err.println("링크 추출 중 오류 발생: " + e.getMessage());
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

        // 알람 테스트 코드 - 시작
        notificationService.sendNotification(
                NotificationDto.builder()
                        .title("임시 알람 제목")
                        .link("https://ncms.ulsan.ac.kr/cicweb/1024")
                        .build());
        // 알람 테스트 코드 - 끝

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

        log.info("resent post update fin");

        if (!noticesToSave.isEmpty()) {
            noticeRepository.saveAll(noticesToSave);

            if (NOTIFICATION) {
                for (Notice notice : noticesToSave) {
                    notificationService.sendNotification(
                            NotificationDto.builder()
                                    .title(notice.getTitle())
                                    .link(notice.getLink())
                                    .build());
                }
            } else {
                NOTIFICATION = true;
            }

            log.info(recentIds.toString());
        }
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
        PageRequest pageRequest = PageRequest.of(page, 10,
                Sort.by(Sort.Order.asc("category"), Sort.Order.desc("id"))
        );

        if (categoryInt == 0) {
            category = Category.NOTICE;
        } else if (categoryInt == 1) {
            category = Category.COMMON;
        } else {
            return noticeRepository.findAll(pageRequest);
        }

        return noticeRepository.findAllByCategory(category, pageRequest);
    }

    // 검색 기능
    @Override
    public Page<Notice> getNoticeByKeyWord(String keyWord, Integer page) {

        PageRequest pageRequest = PageRequest.of(page, 10,
                Sort.by(Sort.Order.asc("category"), Sort.Order.desc("id"))
        );

        return noticeRepository.findNoticeByTitleContainingIgnoreCase(keyWord, pageRequest);
    }
}
