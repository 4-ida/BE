package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.BookmarkDeleteResponse;
import com.pillmate.pillmate.DTO.BookmarkListResponse;
import com.pillmate.pillmate.DTO.BookmarkResponse;
import com.pillmate.pillmate.DTO.DrugDetailResponse;
import com.pillmate.pillmate.Domain.Bookmark;
import com.pillmate.pillmate.Repository.BookmarkRepository;
import com.pillmate.pillmate.Util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final DrugDetailService drugDetailService; // 외부(식약처) 조회 진입점

    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /** 북마크 추가 (멱등) */
    @Transactional
    public BookmarkResponse addBookmark(String drugId) {
        Long userId = SecurityUtil.currentUserId();

        Bookmark bookmark = bookmarkRepository
                .findByUserIdAndDrugId(userId, drugId)
                .orElseGet(() -> bookmarkRepository.save(
                        Bookmark.builder()
                                .userId(userId)
                                .drugId(drugId)
                                .bookmarkedAt(Instant.now())
                                .build()
                ));

        return BookmarkResponse.builder()
                .drugId(drugId)
                .bookmarkedAt(ISO_INSTANT.format(bookmark.getBookmarkedAt()))
                .build();
    }

    /** 북마크 목록 조회 (식약처 OPEN API 기반, 캐시 없음 버전) */
    @Transactional(readOnly = true)
    public BookmarkListResponse listBookmarks(Integer page, Integer size, String sort) {
        Long userId = SecurityUtil.currentUserId();

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;

        String sortKey = (sort == null || sort.isBlank()) ? "recent" : sort.trim().toLowerCase();
        Sort springSort = Sort.by(Sort.Direction.DESC, "bookmarkedAt");
        PageRequest pr = PageRequest.of(p, s, springSort);

        Page<Bookmark> pageResult = bookmarkRepository.findByUserId(userId, pr);

        List<BookmarkListResponse.Item> items = new ArrayList<>();
        for (Bookmark b : pageResult.getContent()) {
            String drugId = b.getDrugId();
            String name = null;
            String thumbnailUrl = null;

            try {
                // DrugDetailService의 실제 공개 메서드명을 사용하세요.
                // 팀 코드 기준 일반적으로 getDetail(String) 입니다.
                DrugDetailResponse detail = drugDetailService.fetchDrugDetail(drugId);
                if (detail != null) {
                    name = detail.getName();
                    if (detail.getImages() != null && !detail.getImages().isEmpty()) {
                        thumbnailUrl = detail.getImages().get(0);
                    }
                }
            } catch (Exception ignore) {
                // 외부 API 실패 시 해당 아이템만 빈 값으로 내려가도록 한다.
            }

            items.add(BookmarkListResponse.Item.builder()
                    .drugId(drugId)
                    .name(name)
                    .thumbnailUrl(thumbnailUrl)
                    .bookmarkedAt(ISO_INSTANT.format(b.getBookmarkedAt())) // 문자열 ISO-8601
                    .build());
        }

        return BookmarkListResponse.builder()
                .page(p)
                .size(s)
                .sort(sortKey)
                .totalElements(pageResult.getTotalElements())
                .items(items)
                .build();
    }

    /** 북마크 삭제 */
    @Transactional
    public BookmarkDeleteResponse removeBookmark(String drugId) {
        Long userId = SecurityUtil.currentUserId();

        Bookmark bookmark = bookmarkRepository.findByUserIdAndDrugId(userId, drugId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + drugId));

        bookmarkRepository.delete(bookmark);

        return BookmarkDeleteResponse.builder()
                .deleted(true)
                .drugId(drugId)
                .deletedAt(ISO_INSTANT.format(Instant.now()))
                .build();
    }
}
