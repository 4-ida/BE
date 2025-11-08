package com.pillmate.pillmate.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "drug_manual_overrides")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugManualOverride {

    @Id
    @Column(length = 32, nullable = false)
    private String drugId;

    @Column(length = 255)
    private String strength;

    @Column(name = "ingredients_csv", length = 1000)
    private String ingredientsCsv;

    public List<String> getIngredients() {
        if (!StringUtils.hasText(ingredientsCsv)) {
            return Collections.emptyList();
        }
        return Arrays.stream(ingredientsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}

