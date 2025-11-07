package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.Domain.Bookmark;
import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.DTO.BookmarkResponse;
import com.pillmate.pillmate.Repository.BookmarkRepository;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.Util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final DrugRepository drugRepository;

    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /**
     * 북마크 추가 (idempotent)
     * 이미 존재하면 기존 것을 그대로 응답
     */
    @Transactional
    public BookmarkResponse addBookmark(String drugId) {
        Long userId = SecurityUtil.currentUserId();

        // 약 존재 여부 확인
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다. drugId=" + drugId));

        // 이미 있으면 그대로 응답(멱등성)
        Bookmark bookmark = bookmarkRepository
                .findByUserIdAndDrugId(userId, drugId)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Bookmark b = Bookmark.builder()
                            .userId(userId)
                            .drugId(drugId)
                            .bookmarkedAt(Instant.now()) // UTC
                            .build();
                    return bookmarkRepository.save(b);
                });

        return BookmarkResponse.builder()
                .drugId(drugId)
                .name(drug.getName())
                .thumbnailUrl(drug.getThumbnailUrl())
                .bookmarkedAt(ISO_INSTANT.format(bookmark.getBookmarkedAt()))
                .build();
    }
}
