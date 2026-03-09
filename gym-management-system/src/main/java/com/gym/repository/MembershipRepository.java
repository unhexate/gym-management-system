package com.gym.repository;

import com.gym.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByMemberId(Long memberId);
    Optional<Membership> findTopByMemberIdAndStatusOrderByEndDateDesc(Long memberId, String status);
}
