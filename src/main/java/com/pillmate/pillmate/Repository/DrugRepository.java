package com.pillmate.pillmate.Repository;

import com.pillmate.pillmate.Domain.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRepository extends JpaRepository<Drug, String> {

    // ====== 자동완성용 ======

    // 1) 정확 일치
    @Query("""
        select d from Drug d
        where d.active = true
          and (
               lower(coalesce(d.name, '')) = lower(:q)
            or lower(coalesce(d.genericName, '')) = lower(:q)
          )
        order by coalesce(d.popularityScore, 0) desc
        """)
    List<Drug> findExact(@Param("q") String q);

    // 2) 접두 일치
    @Query("""
        select d from Drug d
        where d.active = true
          and (
               lower(coalesce(d.name, '')) like concat(lower(:q), '%')
            or lower(coalesce(d.genericName, '')) like concat(lower(:q), '%')
          )
        order by coalesce(d.popularityScore, 0) desc
        """)
    List<Drug> findPrefix(@Param("q") String q);

    // 3) 부분 포함
    @Query("""
        select d from Drug d
        where d.active = true
          and (
               lower(coalesce(d.name, '')) like concat('%', lower(:q), '%')
            or lower(coalesce(d.genericName, '')) like concat('%', lower(:q), '%')
          )
        order by coalesce(d.popularityScore, 0) desc
        """)
    List<Drug> findSubstring(@Param("q") String q);

    // ====== 검색(페이지네이션 + 유사도 정렬) ======
    @Query(
        value = """
            select d from Drug d
            where d.active = true
              and (
                   lower(coalesce(d.name, ''))        like concat('%', lower(:q), '%')
                or lower(coalesce(d.genericName, '')) like concat('%', lower(:q), '%')
              )
            order by
              case
                when lower(coalesce(d.name, ''))        like concat(lower(:q), '%')
                  or lower(coalesce(d.genericName, '')) like concat(lower(:q), '%')
                  then 0
                when lower(coalesce(d.name, ''))        like concat('% ', lower(:q), '%')
                  or lower(coalesce(d.genericName, '')) like concat('% ', lower(:q), '%')
                  then 1
                else 2
              end asc,
              coalesce(d.popularityScore, 0) desc,
              length(coalesce(d.name, '')) asc
            """,
        countQuery = """
            select count(d) from Drug d
            where d.active = true
              and (
                   lower(coalesce(d.name, ''))        like concat('%', lower(:q), '%')
                or lower(coalesce(d.genericName, '')) like concat('%', lower(:q), '%')
              )
            """
    )
    Page<Drug> searchByNameRelevance(@Param("q") String q, Pageable pageable);
}
