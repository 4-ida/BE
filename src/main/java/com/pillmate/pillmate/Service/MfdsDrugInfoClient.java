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
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugBody;
import com.pillmate.pillmate.Service.dto.MfdsEasyDrugResponse.MfdsEasyDrugItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsDrugInfoClient {

    private static final String EASY_DRUG_PATH = "/DrbEasyDrugInfoService/getDrbEasyDrugList";

    private final RestTemplate restTemplate;
    private final MfdsApiProperties properties;

    public Optional<MfdsEasyDrugItem> fetchEasyDrug(String itemSeq) {
        if (!StringUtils.hasText(itemSeq)) {
            return Optional.empty();
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .path(EASY_DRUG_PATH)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("type", "json")
                .queryParam("itemSeq", itemSeq.trim())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .build(true)
                .toUri();

        try {
            ResponseEntity<MfdsEasyDrugResponse> response =
                    restTemplate.getForEntity(uri, MfdsEasyDrugResponse.class);
            MfdsEasyDrugResponse body = response.getBody();

            if (body == null || body.getBody() == null) {
                log.warn("MFDS easy drug response body is empty for itemSeq={}", itemSeq);
                return Optional.empty();
            }

            if (!"00".equals(body.getHeader().getResultCode())) {
                log.warn("MFDS easy drug response error. code={}, message={}",
                        body.getHeader().getResultCode(), body.getHeader().getResultMsg());
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

