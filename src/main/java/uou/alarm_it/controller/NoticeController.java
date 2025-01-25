package uou.alarm_it.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uou.alarm_it.apiPayload.ApiResponse;
import uou.alarm_it.apiPayload.code.status.SuccessStatus;
import uou.alarm_it.domain.Notice;
import uou.alarm_it.service.NoticeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 25.01.18
     * 작성자 : 류기현
     * webCrawling Test Api
     */
    @GetMapping("/crawling")
    public ApiResponse<List<Notice>> webCrawling(
            @RequestParam(name = "maxPage", defaultValue = "1") Integer maxPage
    ) {
        return ApiResponse.onSuccess(noticeService.webCrawling(maxPage));
    }

    /**
     * 25.01.18
     * 작성자 : 류기현
     * 카테고리별 공지 조회
     */
    @GetMapping("/notice")
    public ApiResponse<Page<Notice>> getNoticesByCategory(
            @RequestParam(name = "category", defaultValue = "2") Integer category,
            @RequestParam(name = "page", defaultValue = "0") Integer page
    ) {
        return ApiResponse.onSuccess(noticeService.getNoticeList(category, page));

    }

    /**
     * 25.01.25
     * 작성자 : 류기현
     * 전체 데이터 크롤링 후 저장
     */
    @GetMapping("/refresh")
    public ApiResponse<SuccessStatus> wholeCrawlingSave(
            @RequestParam(name = "maxPage", defaultValue = "1") Integer maxPage
    ) {
        noticeService.refresh(maxPage);
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }
}
