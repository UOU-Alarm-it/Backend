package uou.alarm_it.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uou.alarm_it.apiPayload.ApiResponse;
import uou.alarm_it.apiPayload.code.status.SuccessStatus;
import uou.alarm_it.notice.domain.Enum.Major;
import uou.alarm_it.notice.domain.Notice;
import uou.alarm_it.notice.service.NoticeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    private final SseEmitter emitter = new SseEmitter();

    /**
     * 25.01.18
     * 작성자 : 류기현
     * 카테고리별 공지 조회
     */
    @GetMapping("/notice")
    @Validated
    public ApiResponse<Page<Notice>> getNoticesByCategory(
            @RequestParam(name = "type", defaultValue = "전체") String type,
            @RequestParam(name = "major", defaultValue = "ICT융합학부") String major,
            @RequestParam(name = "page", defaultValue = "0") Integer page

    ) {
        return ApiResponse.onSuccess(noticeService.getNoticeList(type, major, page));
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

    /**
     * 25.01.28
     * 작성자 : 류기현
     * 검색 API
     */
    @GetMapping("/search")
    public ApiResponse<Page<Notice>> search(
            @RequestParam(name = "keyWord", defaultValue = "") String keyWord,
            @RequestParam(name = "major", defaultValue = "ICT융합학부") String major,
            @RequestParam(name = "page", defaultValue = "0") Integer page

    ) {

        return ApiResponse.onSuccess(noticeService.getNoticeByKeyWord(keyWord, major, page));
    }
}
