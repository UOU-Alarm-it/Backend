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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notice.domain.Enum.Type;
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
    private final NotificationService notificationService;

    private static final Set<Long> recentITIds = new HashSet<>(); // 최근 크롤링한 ID
    private static final Set<Long> recentAIIds = new HashSet<>();
    private static final Set<Long> recentICTIds = new HashSet<>();

    private static final boolean USE_LOCAL_HTML = false; // true: 로컬 HTML 테스트 모드, false: 웹 크롤링 모드
    public static boolean NOTIFICATION = false;


    // 웹 크롤링
    @Override
    public List<Notice> webCrawling(Integer page, Major major) {

        String baseURL;

        switch (major) {
            case AI융합전공 -> baseURL = "https://ncms.ulsan.ac.kr/cicweb/1024";
            case IT융합전공 -> baseURL = "https://ai.ulsan.ac.kr/ai/1105";
            case ICT융합학부 -> baseURL = "https://ict.ulsan.ac.kr/ict/5786";
            default -> throw new IllegalStateException("Unexpected value: " + major);
        }

        List<Notice> noticeList = new ArrayList<>();

        int j = 0;

        for (int i = 1; page >= i; i++) {
            String pageStr = Integer.toString(i);
            String IT_URL = baseURL + "?pageIndex=" + pageStr + "&bbsId=1637&searchCondition=title&searchKeyword=";

            Elements contents;

            if (USE_LOCAL_HTML) {
                // 로컬 HTML 파일을 이용한 테스트 모드
                log.info("로컬 html 파일 test 모드");
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

                if (content.select("td").text().equals("등록된 게시물이 없습니다.")) {
                    break;
                }

                long id;
                String title;
                LocalDate date = null;
                String link = null;
                Type type;
                String idString;

                // 공지인 경우, 아닌경우 title, id, category 추출
                if (content.hasClass("noti")) {
                    title = content.select("tr td.bdlTitle b a").text();                                // title
                    idString = content.select("tr td.bdlTitle b a").attr("href");        // id
                    type = Type.NOTICE;                                                                  // category
                } else {
                    title = content.select("td.bdlTitle a").text();
                    idString = content.select("tr td.bdlTitle a").attr("href");
                    type = Type.COMMON;
                }

                if (idString.isEmpty()) {
                    throw new IllegalArgumentException("idString 이 null 이거나 비어 있습니다." + title);
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
                    link = baseURL + link;
                } catch (Exception e) {
                    System.err.println("링크 추출 중 오류 발생: " + e.getMessage());
                }



                Notice notice = Notice.builder()
                        .id(id)
                        .title(title)
                        .date(date)
                        .link(link)
                        .type(type)
                        .major(major)
                        .build();
                noticeList.add(notice);
            }
        }

        return noticeList;
    }

    @Override
    public void crawlAndSave(Integer page, Major major) {

        // major 별 처리
        Set<Long> recentIds = grantRecentIds(major);

        List<Notice> notices = webCrawling(1, major);
        Set<Long> crawlIds = notices.stream().map(Notice::getId).collect(Collectors.toSet());



        Set<Long> willSaveIds = diffWithSaved(crawlIds, recentIds, major);
        List<Notice> noticesToSave = new ArrayList<>();

        log.info("will save Ids : {}", willSaveIds.toString());

        for (Long id : willSaveIds) {
            notices.stream()
                    .filter(notice -> notice.getId().equals(id))
                    .findFirst()
                    .ifPresent(noticesToSave::add);
        }

        if (!noticesToSave.isEmpty()) {
            log.info("noticeToSave is not empty");
            noticeRepository.saveAll(noticesToSave);

            if (NOTIFICATION) {
                for (Notice notice : noticesToSave) {
                    notificationService.sendNotification(
                            NotificationDto.builder()
                                    .title(notice.getTitle())
                                    .link(notice.getLink())
                                    .major(major.toString())
                                    .build());
                }
            }

            log.info(recentIds.toString());
        }

        log.info("resent post update fin : scheduledUpdate");
    }

    // 공지 클롤링, 저장 (전체 페이지)
    @Override
    @Transactional
    public void refresh(Integer page) {

        List<Notice> notices = new ArrayList<>();

        notices.addAll(webCrawling(page, Major.IT융합전공));
        notices.addAll(webCrawling(page, Major.AI융합전공));
        notices.addAll(webCrawling(page, Major.ICT융합학부));

        noticeRepository.deleteAll();
        noticeRepository.saveAll(notices);
    }

    // id 의 차이를 구함
    public Set<Long> diffWithSaved(Set<Long> crawlIds, Set<Long> presentIds, Major major) {

        Set<Long> wouldSaveIds = new HashSet<>();

        Set<Long> recentIds = grantRecentIds(major);

        // id를 가지고 있지 않다면, id 를 wouldSaveIds 에 추가
        for (Long id : crawlIds) {
            if (!presentIds.contains(id)) {
                wouldSaveIds.add(id);
            }
        }

        // resentIds hashSet 최신화
        if (!wouldSaveIds.isEmpty()) {
            log.info("최신과 차이가 있는 recentId : {}", recentIds);
            recentIds.clear();
            recentIds.addAll(crawlIds);
        }

        return wouldSaveIds;
    }

    public Set<Long> grantRecentIds(Major major) {

        Set<Long> recentIds;

        switch (major) {
            case IT융합전공 -> recentIds = recentITIds;
            case AI융합전공 -> recentIds = recentAIIds;
            case ICT융합학부 -> recentIds = recentICTIds;
            default -> throw new IllegalStateException("Unexpected value: " + major);
        }

        return recentIds;
    }

    // 공지 조회
    @Override
    public Page<Notice> getNoticeList(Integer categoryInt, Integer page) {

        Type type;
        PageRequest pageRequest = PageRequest.of(page, 10,
                Sort.by(Sort.Order.asc("category"), Sort.Order.desc("id"))
        );

        if (categoryInt == 0) {
            type = Type.NOTICE;
        } else if (categoryInt == 1) {
            type = Type.COMMON;
        } else {
            return noticeRepository.findAll(pageRequest);
        }

        return noticeRepository.findAllByType(type, pageRequest);
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
