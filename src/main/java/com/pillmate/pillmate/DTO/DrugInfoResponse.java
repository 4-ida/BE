package com.pillmate.pillmate.DTO;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DrugInfoResponse {
    private String drugId;
    private String name;
    private List<String> ingredient;
    private String form;
    private String strength;
    private String rxType;
    private Caution caution;
    private List<String> warnings;
    private boolean bookmarked;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Caution {
        private String alcohol;
        private String caffeine;
    }
}
