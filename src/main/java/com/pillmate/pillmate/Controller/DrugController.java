package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.BookmarkListResponse;
import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.DTO.BookmarkResponse;
import com.pillmate.pillmate.DTO.DrugInfoResponse;
import com.pillmate.pillmate.DTO.InteractionResponse;
import com.pillmate.pillmate.Service.BookmarkService;
import com.pillmate.pillmate.DTO.ImageResponse;
import com.pillmate.pillmate.Service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drug")
public class DrugController {

    private final DrugService drugService;
    private final BookmarkService bookmarkService;

    // ---- 약 명 자동완성 ----
    // GET /api/v1/drug/suggest?q=타이&limit=10
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
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(drugService.search(q, page, size));
    }

    // ---- (상세) 북마크된 약물 이미지 조회 ----
    // GET /api/v1/drug/details/{drugId}/images
    @GetMapping("/details/{drugId}/images")
    public ResponseEntity<ImageResponse> images(@PathVariable String drugId) {
        return ResponseEntity.ok(drugService.getDrugImages(drugId));
    }

    // ---- (상세) 북마크된 약물 기본 정보 조회 ----
    // GET /api/v1/drug/details/{drugId}
    @GetMapping("/details/{drugId}")
    public ResponseEntity<DrugInfoResponse> info(@PathVariable String drugId) {
        return ResponseEntity.ok(drugService.getDrugInfo(drugId));
    }

    // ---- (상세) 북마크된 약물 상호작용 정보 조회 ----
    // GET /api/v1/drug/details/{drugId}/interactions
    @GetMapping("/details/{drugId}/interactions")
    public ResponseEntity<InteractionResponse> interactions(@PathVariable String drugId) {
        return ResponseEntity.ok(drugService.getDrugInteractions(drugId));
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

}

