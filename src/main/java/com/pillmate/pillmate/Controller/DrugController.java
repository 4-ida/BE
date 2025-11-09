package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.BookmarkDeleteResponse;
import com.pillmate.pillmate.DTO.BookmarkListResponse;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.DTO.BookmarkResponse;
import com.pillmate.pillmate.DTO.DrugDetailResponse;
import com.pillmate.pillmate.DTO.InteractionResponse;
import com.pillmate.pillmate.Service.DrugDetailService;
import com.pillmate.pillmate.Service.BookmarkService;
import com.pillmate.pillmate.DTO.ImageResponse;
import com.pillmate.pillmate.Service.DrugService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drug")
public class DrugController {

    private final DrugService drugService;
    private final DrugDetailService drugDetailService;
    private final BookmarkService bookmarkService;

    // ---- 약 명 자동완성 ----
    // GET /api/v1/drug/suggest?q=타이&limit=10
    @Operation(summary = "약 명 자동완성", description = "입력 중인 약명 문자열(q)에 대해 자동완성 후보 목록을 제공합니다.")
    @GetMapping("/suggest")
    public ResponseEntity<SuggestResponse> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
    return ResponseEntity.ok(drugService.suggest(q, limit));
    }
    // ---- 약 명 검색 ----
    // GET /api/v1/drug/search?q=타이레놀&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(drugService.search(q, page, size));
    }


    //북마크추가  POST /api/v1/drug/bookmarks/{drugId}
    @PostMapping("/bookmarks/{drugId}")
    public ResponseEntity<BookmarkResponse> addBookmark(@PathVariable String drugId) {
        return ResponseEntity.ok(bookmarkService.addBookmark(drugId));
    }

    //북마크목록조회 GET /api/v1/drug/bookmarks?page={page}&size={size}&sort={sort}
    @GetMapping("/bookmarks")
    public ResponseEntity<BookmarkListResponse> listBookmarks(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "sort", defaultValue = "recent") String sort
    ) {
        return ResponseEntity.ok(bookmarkService.listBookmarks(page, size, sort));
    }

    //북마크삭제 DELETE /api/v1/drug/bookmarks/{drugId}
    @DeleteMapping("/bookmarks/{drugId}")
    public ResponseEntity<BookmarkDeleteResponse> deleteBookmark(@PathVariable String drugId) {
        return ResponseEntity.ok(bookmarkService.removeBookmark(drugId));
    }
}

