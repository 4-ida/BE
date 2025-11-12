package com.pillmate.pillmate.Service;

import java.net.URI;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.pillmate.pillmate.Config.MfdsApiProperties;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugBody;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsDrugInfoClient {

    // 쉬운약 조회/검색 API 경로
    private static final String EASY_DRUG_PATH = "/DrbEasyDrugInfoService/getDrbEasyDrugList";

    private final RestTemplate restTemplate;
    private final MfdsApiProperties properties;

    /** 제품명으로 검색 (페이지네이션) */
    public Optional<MfdsEasyDrugResponse> searchByName(String itemName, int page, int size) {
        // API 키 검증
        if (!StringUtils.hasText(properties.getServiceKey())) {
            log.error("MFDS_SERVICE_KEY is not set! Please set the environment variable MFDS_SERVICE_KEY");
            return Optional.empty();
        }

        int pageNo   = Math.max(1, page + 1);
        int numOfRows = Math.max(1, size);

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getBaseUrl()) // fromHttpUrl 경고 피하려면 fromUriString 사용
                .path(EASY_DRUG_PATH)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("type", "json")
                .queryParam("itemName", itemName)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            ResponseEntity<MfdsEasyDrugResponse> res =
                    restTemplate.getForEntity(uri, MfdsEasyDrugResponse.class);

            MfdsEasyDrugResponse body = res.getBody();
            if (body == null) {
                log.warn("MFDS search empty response body for q={}", itemName);
                return Optional.empty();
            }
            
            // Header null 체크 추가
            if (body.getHeader() == null) {
                log.warn("MFDS search response header is null for q={}", itemName);
                return Optional.empty();
            }
            
            // Body null 체크
            if (body.getBody() == null) {
                log.warn("MFDS search empty body for q={}", itemName);
                return Optional.empty();
            }
            
            // Result code 체크
            String resultCode = body.getHeader().getResultCode();
            if (resultCode == null || !"00".equals(resultCode)) {
                log.warn("MFDS search error: code={}, msg={}",
                        resultCode, body.getHeader().getResultMsg());
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (Exception ex) {
            log.error("MFDS search API failed for q={}", itemName, ex);
            return Optional.empty();
        }
    }

    /** 품목기준코드(=itemSeq)로 단건 조회 */
    public Optional<MfdsEasyDrugItem> fetchEasyDrug(String itemSeq) {
        if (!StringUtils.hasText(itemSeq)) {
            return Optional.empty();
        }

        // API 키 검증
        if (!StringUtils.hasText(properties.getServiceKey())) {
            log.error("MFDS_SERVICE_KEY is not set! Please set the environment variable MFDS_SERVICE_KEY");
            return Optional.empty();
        }

        URI uri = UriComponentsBuilder
                .fromUriString(properties.getBaseUrl())
                .path(EASY_DRUG_PATH)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("type", "json")
                .queryParam("itemSeq", itemSeq.trim())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            ResponseEntity<MfdsEasyDrugResponse> response =
                    restTemplate.getForEntity(uri, MfdsEasyDrugResponse.class);

            MfdsEasyDrugResponse body = response.getBody();
            if (body == null) {
                log.warn("MFDS easy drug response body is null for itemSeq={}", itemSeq);
                return Optional.empty();
            }
            
            // Header null 체크 추가
            if (body.getHeader() == null) {
                log.warn("MFDS easy drug response header is null for itemSeq={}", itemSeq);
                return Optional.empty();
            }
            
            // Body null 체크
            if (body.getBody() == null) {
                log.warn("MFDS easy drug response body is empty for itemSeq={}", itemSeq);
                return Optional.empty();
            }
            
            // Result code 체크
            String resultCode = body.getHeader().getResultCode();
            if (resultCode == null || !"00".equals(resultCode)) {
                log.warn("MFDS easy drug response error. code={}, message={}",
                        resultCode, body.getHeader().getResultMsg());
                return Optional.empty();
            }

            MfdsEasyDrugBody bodyData = body.getBody();
            List<MfdsEasyDrugItem> items = bodyData.getItems();
            if (items == null || items.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(items.get(0));
        } catch (Exception ex) {
            log.error("MFDS easy drug API call failed for itemSeq={}", itemSeq, ex);
            return Optional.empty();
        }
    }
}

