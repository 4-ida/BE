package com.pillmate.pillmate.DTO;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class InteractionResponse {
    private String drugId;
    private Interactions interactions;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Interactions {
        private List<Beverage> beverage; // 카페인/알코올 등
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Beverage {
        private String target;              // "카페인" | "알코올"
        private String recommendation;      // 권고 문구
        private SafeWindow safeWindow;      // 안전 시간
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SafeWindow {
        private int beforeMinutes;
        private int afterMinutes;
    }
}
