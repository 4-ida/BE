package com.pillmate.pillmate.Service;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.pillmate.pillmate.Config.MfdsApiProperties;
import com.pillmate.pillmate.Service.dto.MfdsIngredientResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsDrugIngredientClient {

    private static final String INGREDIENT_PATH = "/DrbItemIngrRcpeInfoService/getDrbItemIngrRcpeList";

    private final RestTemplate restTemplate;
    private final MfdsApiProperties properties;

    public List<MfdsIngredientResponse.Item> fetchIngredients(String itemSeq) {
        // API 키 검증
        if (!StringUtils.hasText(properties.getServiceKey())) {
            log.error("MFDS_SERVICE_KEY is not set! Please set the environment variable MFDS_SERVICE_KEY");
            return List.of();
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl())
                .path(INGREDIENT_PATH)
                .queryParam("serviceKey", properties.getServiceKey())
                .queryParam("type", "json")
                .queryParam("itemSeq", itemSeq)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .build(true)
                .toUri();

        try {
            ResponseEntity<MfdsIngredientResponse> response =
                    restTemplate.getForEntity(uri, MfdsIngredientResponse.class);

            MfdsIngredientResponse body = response.getBody();
            if (body == null || body.getBody() == null) {
                log.warn("MFDS ingredient response body empty for itemSeq={}", itemSeq);
                return List.of();
            }

            if (!"00".equals(body.getHeader().getResultCode())) {
                log.warn("MFDS ingredient response error. code={}, message={}",
                        body.getHeader().getResultCode(), body.getHeader().getResultMsg());
                return List.of();
            }

            return body.getBody().getItems();
        } catch (Exception ex) {
            log.error("MFDS ingredient API call failed for itemSeq={}", itemSeq, ex);
            return List.of();
        }
    }
}



