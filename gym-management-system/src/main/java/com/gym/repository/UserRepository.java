package com.gym.repository;

import com.gym.model.User;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR UPPER(u.role) = UPPER(:role))
              AND (:query IS NULL
                   OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY u.name ASC
            """)
    List<User> searchUsers(@Param("role") String role,
                           @Param("query") String query,
                           Pageable pageable);

    @Query(value = """
            SELECT id, name, email, role
            FROM users
            WHERE role = :role
            ORDER BY name ASC
            """, nativeQuery = true)
    List<UserLookupProjection> findLookupByRole(@Param("role") String role);
}
