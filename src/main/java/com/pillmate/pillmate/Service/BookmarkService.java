package com.pillmate.pillmate.Service;

import com.pillmate.pillmate.DTO.BookmarkDeleteResponse;
import com.pillmate.pillmate.DTO.BookmarkListResponse;
import com.pillmate.pillmate.DTO.BookmarkResponse;
import com.pillmate.pillmate.Domain.Bookmark;
import com.pillmate.pillmate.Domain.Drug;
import com.pillmate.pillmate.Repository.BookmarkRepository;
import com.pillmate.pillmate.Repository.DrugRepository;
import com.pillmate.pillmate.Util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final DrugRepository drugRepository;
    


    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /** 북마크 추가 (멱등) */
    @Transactional
    public BookmarkResponse addBookmark(String drugId) {
        Long userId = SecurityUtil.currentUserId(); //  정적 메서드로 통일

        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약물입니다. drugId=" + drugId));

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
                .name(drug.getName())
                .thumbnailUrl(drug.getThumbnailUrl())
                .bookmarkedAt(ISO_INSTANT.format(bookmark.getBookmarkedAt()))
                .build();
    }

    /** 북마크 목록 조회 */
    @Transactional(readOnly = true)
    public BookmarkListResponse listBookmarks(Integer page, Integer size, String sort) {
        Long userId = SecurityUtil.currentUserId(); //  정적 메서드로 통일

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0 || size > 100) ? 20 : size;

        String sortKey = (sort == null || sort.isBlank()) ? "recent" : sort.trim().toLowerCase();
        // 현재는 recent만 지원 → bookmarkedAt DESC
        Sort springSort = Sort.by(Sort.Direction.DESC, "bookmarkedAt");

        PageRequest pr = PageRequest.of(p, s, springSort);
        Page<Bookmark> pageResult = bookmarkRepository.findByUserId(userId, pr);

        // Drug 정보를 배치로 끌어와서 이름/썸네일을 붙임
        List<String> drugIds = pageResult.getContent().stream()
                .map(Bookmark::getDrugId)
                .toList();

        Map<String, Drug> drugMap = drugRepository.findAllById(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        List<BookmarkListResponse.Item> items = pageResult.getContent().stream()
                .map(b -> {
                    Drug d = drugMap.get(b.getDrugId());
                    return BookmarkListResponse.Item.builder()
                            .drugId(b.getDrugId())
                            .name(d != null ? d.getName() : null)
                            .thumbnailUrl(d != null ? d.getThumbnailUrl() : null)
                            // DTO가 OffsetDateTime이라면 변환, 문자열이면 ISO로 포맷하세요.
                            // 예1) DTO가 OffsetDateTime:
                            .bookmarkedAt(b.getBookmarkedAt().atOffset(ZoneOffset.UTC))
                            // 예2) DTO가 String이라면 아래로 교체:
                            // .bookmarkedAt(ISO_INSTANT.format(b.getBookmarkedAt()))
                            .build();
                })
                .toList();

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
    // 인증된 사용자 ID 확보 (프로젝트에 맞춰 사용: getCurrentUserIdOrThrow / currentUserId 등)
        Long userId = SecurityUtil.currentUserId();

    // 존재 체크
        Bookmark bookmark = bookmarkRepository.findByUserIdAndDrugId(userId, drugId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + drugId));

    // 물리 삭제(soft delete 필요 시 엔티티 확장)
        bookmarkRepository.delete(bookmark);

        return BookmarkDeleteResponse.builder()
                .deleted(true)
                .drugId(drugId)
                .deletedAt(ISO_INSTANT.format(java.time.Instant.now())) // 서비스 상단의 ISO_INSTANT 재사용
                .build();
    }



}

