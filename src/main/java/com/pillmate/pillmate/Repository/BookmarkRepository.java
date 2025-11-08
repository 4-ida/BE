package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserIdAndDrugId(Long userId, String drugId);

    boolean existsByUserIdAndDrugId(Long userId, String drugId);

    Page<Bookmark> findByUserId(Long userId, Pageable pageable);

}
