package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Drug;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRepository extends JpaRepository<Drug, String> {

    // 정확 일치
    @Query("""
        select d from Drug d
        where d.active = true and (lower(d.name) = lower(:q) or lower(d.genericName) = lower(:q))
        order by coalesce(d.popularityScore,0) desc
        """)
    List<Drug> findExact(@Param("q") String q);

    // 접두 일치
    @Query("""
        select d from Drug d
        where d.active = true and (
              lower(d.name) like concat(lower(:q), '%')
           or lower(d.genericName) like concat(lower(:q), '%')
        )
        order by coalesce(d.popularityScore,0) desc
        """)
    List<Drug> findPrefix(@Param("q") String q);

    // 부분 포함
    @Query("""
        select d from Drug d
        where d.active = true and (
              lower(d.name) like concat('%', lower(:q), '%')
           or lower(d.genericName) like concat('%', lower(:q), '%')
        )
        order by coalesce(d.popularityScore,0) desc
        """)
    List<Drug> findSubstring(@Param("q") String q);
}
