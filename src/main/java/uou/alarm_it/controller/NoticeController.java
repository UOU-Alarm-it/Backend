package uou.alarm_it.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uou.alarm_it.apiPayload.ApiResponse;
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
    )
    {
        return ApiResponse.onSuccess(noticeService.webCrawling(maxPage));
    }
}
