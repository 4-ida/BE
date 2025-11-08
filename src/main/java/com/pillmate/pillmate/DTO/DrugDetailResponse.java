package com.pillmate.pillmate.DTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DrugDetailResponse {
    private final String drugId;
    private final String name;
    private final String entpName;
    private final String rxType;
    private final String strength;
    private final List<String> ingredients;
    private final String efficacy;
    private final String dosage;
    private final String cautions;
    private final Map<String, String> cautionsSummary;
    private final List<String> images;
    private final OffsetDateTime retrievedAt;
}

