package com.pillmate.pillmate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAvailabilityResponse {

    private String message;
    private boolean success;
    private Data data;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String email;
        private boolean isAvailable;
    }
}

