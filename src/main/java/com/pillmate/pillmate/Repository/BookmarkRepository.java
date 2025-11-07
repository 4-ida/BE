package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserIdAndDrugId(Long userId, String drugId);

    boolean existsByUserIdAndDrugId(Long userId, String drugId);
}
