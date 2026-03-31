package com.gym.repository;

import com.gym.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = """
            SELECT id, name, email, role
            FROM users
            WHERE (:role IS NULL OR UPPER(role) = UPPER(:role))
              AND (:query IS NULL
                   OR LOWER(name) LIKE LOWER('%' || :query || '%')
                   OR LOWER(email) LIKE LOWER('%' || :query || '%'))
            ORDER BY name ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<UserLookupProjection> searchUsers(@Param("role") String role,
                                           @Param("query") String query,
                                           @Param("limit") int limit);

    @Query(value = """
            SELECT id, name, email, role
            FROM users
            WHERE email = :email
            LIMIT 1
            """, nativeQuery = true)
    Optional<UserLookupProjection> findLookupByEmail(@Param("email") String email);

    @Query(value = """
            SELECT id, name, email, role
            FROM users
            WHERE role = :role
            ORDER BY name ASC
            """, nativeQuery = true)
    List<UserLookupProjection> findLookupByRole(@Param("role") String role);
}
