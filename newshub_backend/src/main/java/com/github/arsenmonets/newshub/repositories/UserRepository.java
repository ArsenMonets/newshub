package com.github.arsenmonets.newshub.repositories;

import com.github.arsenmonets.newshub.models.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByLogin(String login);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.role IN ('AUTHOR', 'ADMIN')")
    List<UserEntity> findAllAuthors();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM UserEntity u JOIN u.subscribedCategories c WHERE u.id = :userId AND c.id = :categoryId")
    boolean isSubscribedToCategory(Long userId, Long categoryId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM UserEntity u JOIN u.subscribedAuthors a WHERE u.id = :userId AND a.id = :authorId")
    boolean isSubscribedToAuthor(Long userId, Long authorId);

    @Query("SELECT u FROM UserEntity u WHERE u.role != 'ADMIN' AND u.login LIKE %:loginFilter%")
    Page<UserEntity> findAllNonAdminsWithLoginFilter(String loginFilter, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM user_categories WHERE category_id = :categoryId", nativeQuery = true)
    void removeUserCategorySubscription(Long categoryId);
}
