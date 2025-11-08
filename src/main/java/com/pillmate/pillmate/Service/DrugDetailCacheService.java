//북마크 목록조회
package com.pillmate.pillmate.Service;

import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DrugDetailCacheService {

    @Getter
    @AllArgsConstructor
    public static class DrugSummary {
        private final String name;
        private final String thumbnailUrl;
    }

    private final Map<String, DrugSummary> cache = new ConcurrentHashMap<>();

    public Optional<DrugSummary> get(String drugId) {
        return Optional.ofNullable(cache.get(drugId));
    }

    public void put(String drugId, DrugSummary summary) {
        cache.put(drugId, summary);
    }

    public void evict(String drugId) {
        cache.remove(drugId);
    }
}
