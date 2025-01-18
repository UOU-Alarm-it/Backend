package uou.alarm_it.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import uou.alarm_it.domain.Enum.Category;
import uou.alarm_it.domain.Notice;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Override
    public List<Notice> webCrawling(Integer page) {

        // 웹 크롤링
        List<Notice> noticeList = new ArrayList<>();

        for (int i = 1; page >= i; i++) {
            String pageStr = Integer.toString(i);
            String IT_URL = "https://ncms.ulsan.ac.kr/cicweb/1024?pageIndex=" + pageStr + "&bbsId=1637&searchCondition=title&searchKeyword=";

            Elements contents = null;
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
}
