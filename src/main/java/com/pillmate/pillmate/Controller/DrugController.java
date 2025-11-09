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
import com.pillmate.pillmate.Service.DrugService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(
    name = "drug-controller",
    description = "약물 검색, 자동완성 및 북마크 기능을 제공하는 API입니다."
)
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
    @Operation(
        summary = "약 명 검색",
        description = "약품명을 기준으로 식약처 Open API 및 내부 DB를 통합 검색하여 약물 정보를 반환합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(drugService.search(q, page, size));
    }


    //북마크추가  POST /api/v1/drug/bookmarks/{drugId}
    @Operation(
        summary = "약물 북마크 추가",
        description = "사용자가 특정 약물을 즐겨찾기로 추가합니다. 이미 등록된 경우 중복 추가는 허용되지 않습니다."
    )
    @PostMapping("/bookmarks/{drugId}")
    public ResponseEntity<BookmarkResponse> addBookmark(@PathVariable String drugId) {
        return ResponseEntity.ok(bookmarkService.addBookmark(drugId));
    }

    //북마크목록조회 GET /api/v1/drug/bookmarks?page={page}&size={size}&sort={sort}
    @Operation(
        summary = "북마크 목록 조회",
        description = "사용자가 등록한 모든 약물 북마크 목록을 반환합니다."
    )
    @GetMapping("/bookmarks")
    public ResponseEntity<BookmarkListResponse> listBookmarks(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "sort", defaultValue = "recent") String sort
    ) {
        return ResponseEntity.ok(bookmarkService.listBookmarks(page, size, sort));
    }

    //북마크삭제 DELETE /api/v1/drug/bookmarks/{drugId}
    @Operation(
        summary = "약물 북마크 해제",
        description = "사용자가 등록한 약물 북마크를 삭제합니다."
    )
    @DeleteMapping("/bookmarks/{drugId}")
    public ResponseEntity<BookmarkDeleteResponse> deleteBookmark(@PathVariable String drugId) {
        return ResponseEntity.ok(bookmarkService.removeBookmark(drugId));
    }
}

