package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.DTO.SearchResponse;
import com.pillmate.pillmate.Service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drug")
public class DrugController {

    private final DrugService drugService;

    //  GET /api/v1/drug/suggest?q=타이&limit=10
    @GetMapping("/suggest")
    public ResponseEntity<SuggestResponse> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(drugService.suggest(q, limit));
    }

    //  GET /api/v1/drug/search?q=타이레놀&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(drugService.search(q, page, size));
    }
}

