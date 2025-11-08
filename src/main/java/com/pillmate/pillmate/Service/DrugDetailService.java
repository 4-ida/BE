package com.pillmate.pillmate.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.pillmate.pillmate.DTO.DrugDetailResponse;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugItem;
import com.pillmate.pillmate.Service.dto.MfdsIngredientResponse;
import com.pillmate.pillmate.Service.dto.MfdsPermissionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugDetailService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.KOREA);

    private final MfdsDrugInfoClient mfdsDrugInfoClient;
    private final MfdsDrugIngredientClient mfdsDrugIngredientClient;
    private final MfdsDrugPermissionClient mfdsDrugPermissionClient;

    public DrugDetailResponse fetchDrugDetail(String drugId) {
        MfdsEasyDrugItem item = mfdsDrugInfoClient.fetchEasyDrug(drugId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 품목기준코드의 의약품 정보를 찾을 수 없습니다."
                ));

        List<MfdsIngredientResponse.Item> ingredientItems = mfdsDrugIngredientClient.fetchIngredients(drugId);
        String strength = resolveStrength(ingredientItems);
        List<String> ingredients = ingredientItems.stream()
                .map(MfdsIngredientResponse.Item::getIngredientName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Optional<MfdsPermissionResponse.PermissionItem> permissionOpt = mfdsDrugPermissionClient.fetchPermission(drugId);
        String resolvedRxType = firstNonBlank(
                item.getEtcOtcName(),
                permissionOpt.map(MfdsPermissionResponse.PermissionItem::getResolvedSpcltyPblc).orElse(null),
                permissionOpt.map(MfdsPermissionResponse.PermissionItem::getResolvedEtcOtcCode).orElse(null)
        );

        String cautions = joinSections(
                item.getAtpnWarnQesitm(),
                item.getAtpnQesitm(),
                item.getIntrcQesitm(),
                item.getSeQesitm(),
                item.getDepositMethodQesitm()
        );

        Map<String, String> summary = buildCautionSummary(cautions);

        return DrugDetailResponse.builder()
                .drugId(item.getItemSeq())
                .name(item.getItemName())
                .entpName(item.getEntpName())
                .rxType(withFallback(resolvedRxType, "공식 데이터 미제공"))
                .strength(withFallback(strength, "공식 데이터 미제공"))
                .ingredients(ingredients.isEmpty() ? List.of("공식 데이터 미제공") : ingredients)
                .efficacy(clean(item.getEfcyQesitm()))
                .dosage(clean(item.getUseMethodQesitm()))
                .cautions(clean(cautions))
                .cautionsSummary(summary)
                .images(extractImages(item.getItemImage()))
                .retrievedAt(resolveRetrievedAt(item).orElse(OffsetDateTime.now(KST)))
                .build();
    }

    private String resolveStrength(List<MfdsIngredientResponse.Item> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        List<String> mainStrengths = items.stream()
                .filter(it -> "Y".equalsIgnoreCase(it.getMainIngredient()))
                .map(this::formatStrength)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        List<String> targets = mainStrengths.isEmpty()
                ? items.stream()
                        .map(this::formatStrength)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.toList())
                : mainStrengths;

        if (targets.isEmpty()) {
            return null;
        }
        return String.join(", ", targets);
    }

    private String formatStrength(MfdsIngredientResponse.Item item) {
        String name = item.getIngredientName();
        String content = item.getContent();
        String unit = item.getUnit();

        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(name)) {
            builder.append(name.trim());
        }
        if (StringUtils.hasText(content)) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(content.trim());
        }
        if (StringUtils.hasText(unit)) {
            if (builder.length() > 0) {
                builder.append(unit.startsWith(" ") ? "" : " ");
            }
            builder.append(unit.trim());
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private String withFallback(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String joinSections(String... sections) {
        StringBuilder builder = new StringBuilder();
        for (String section : sections) {
            if (StringUtils.hasText(section)) {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(section.trim());
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private Map<String, String> buildCautionSummary(String cautions) {
        Map<String, String> summary = new LinkedHashMap<>();
        if (!StringUtils.hasText(cautions)) {
            return summary;
        }

        String lower = cautions.toLowerCase(Locale.KOREAN);
        if (lower.contains("음주") || lower.contains("알코올")) {
            summary.put("alcohol", "복용 전후 음주를 피하십시오.");
        }
        if (lower.contains("카페인")) {
            summary.put("caffeine", "카페인 함유 제품과 병용 시 주의하세요.");
        }
        return summary;
    }

    private List<String> extractImages(String itemImage) {
        if (!StringUtils.hasText(itemImage)) {
            return List.of();
        }
        return List.of(itemImage.trim());
    }

    private Optional<OffsetDateTime> resolveRetrievedAt(MfdsEasyDrugItem item) {
        String updateDe = item.getUpdateDe();
        if (StringUtils.hasText(updateDe)) {
            return parseDate(updateDe);
        }
        String openDe = item.getOpenDe();
        if (StringUtils.hasText(openDe)) {
            return parseDate(openDe);
        }
        return Optional.empty();
    }

    private Optional<OffsetDateTime> parseDate(String value) {
        try {
            LocalDate date = LocalDate.parse(value.trim(), BASIC_DATE);
            return Optional.of(date.atStartOfDay(KST).toOffsetDateTime());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String clean(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)&nbsp;", " ")
                .replaceAll("<[^>]+>", "")
                .trim();
    }
}

