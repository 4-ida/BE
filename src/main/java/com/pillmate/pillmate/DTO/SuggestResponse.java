package com.pillmate.pillmate.DTO;


import lombok.*;

import java.util.Collections;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SuggestResponse {

    private String query;
    private List<Suggestion> suggestions;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class Suggestion {
        private String label; // 표시용 문자열
        private String value; // drugId
    }

    public static SuggestResponse empty(String q) {
        return new SuggestResponse(q, Collections.emptyList());
    }
}
