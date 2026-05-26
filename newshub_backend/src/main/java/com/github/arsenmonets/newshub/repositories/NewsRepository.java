package com.github.arsenmonets.newshub.repositories;

import com.github.arsenmonets.newshub.models.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

        @Query("SELECT n FROM NewsEntity n WHERE n.title LIKE %:query% OR n.content LIKE %:query%")
        Page<NewsEntity> searchByKeyword(@Param("query") String query, Pageable pageable);

        @Query("SELECT n FROM NewsEntity n WHERE " +
                        "(:#{#categoryIds.isEmpty()} = true OR n.category.id IN :categoryIds) AND " +
                        "(:#{#authorIds.isEmpty()} = true OR n.author.id IN :authorIds)")
        Page<NewsEntity> filter(@Param("categoryIds") List<Long> categoryIds,
                        @Param("authorIds") List<Long> authorIds,
                        Pageable pageable);

        @Modifying
        @Transactional
        @Query("UPDATE NewsEntity n SET n.category.id = :newCategoryId WHERE n.category.id = :oldCategoryId")
        void updateNewsCategoryId(@Param("oldCategoryId") Long oldCategoryId,
                        @Param("newCategoryId") Long newCategoryId);
}
