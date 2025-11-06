package com.pillmate.pillmate.Controller;

import com.pillmate.pillmate.DTO.SuggestResponse;
import com.pillmate.pillmate.Service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drugs")
public class DrugController {

    private final DrugService drugService;

    // GET /api/v1/drugs/suggest?q=타이&limit=10
    @GetMapping("/suggest")
    public ResponseEntity<SuggestResponse> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(drugService.suggest(q, limit));
    }
}
