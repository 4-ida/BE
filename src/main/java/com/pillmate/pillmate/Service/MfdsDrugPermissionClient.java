package com.pillmate.pillmate.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.pillmate.pillmate.Config.MfdsApiProperties;
import com.pillmate.pillmate.Service.dto.MfdsPermissionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsDrugPermissionClient {

    private static final String PERMISSION_PATH = "/DrugPrdtPrmsnInfoService07/getDrugPrdtPrmsnInq07";

    private final RestTemplate restTemplate;
    private final MfdsApiProperties properties;

    public Optional<MfdsPermissionResponse.PermissionItem> fetchPermission(String itemSeq) {
        // API 키 검증
        if (!StringUtils.hasText(properties.getServiceKey())) {
            log.error("MFDS_SERVICE_KEY is not set! Please set the environment variable MFDS_SERVICE_KEY");
            return Optional.empty();
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .path(PERMISSION_PATH)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("type", "json")
                .queryParam("prdlst_Stdr_code", itemSeq)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .build(true)
                .toUri();

        try {
            ResponseEntity<MfdsPermissionResponse> response =
                    restTemplate.getForEntity(uri, MfdsPermissionResponse.class);

            MfdsPermissionResponse body = response.getBody();
            if (body == null || body.getBody() == null) {
                log.warn("MFDS permission response body empty for itemSeq={}", itemSeq);
                return Optional.empty();
            }

            if (!"00".equals(body.getHeader().getResultCode())) {
                log.warn("MFDS permission response error. code={}, message={}",
                        body.getHeader().getResultCode(), body.getHeader().getResultMsg());
                return Optional.empty();
            }

            List<MfdsPermissionResponse.PermissionItem> items = body.getBody().getItems();
            if (items == null || items.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(items.get(0));
        } catch (Exception ex) {
            log.error("MFDS permission API call failed for itemSeq={}", itemSeq, ex);
            return Optional.empty();
        }
    }
}



