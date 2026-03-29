package com.breadlab.breaddesk.member.repository;

import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.role = :role AND m.active = true")
    List<Member> findByRoleAndActiveTrue(@Param("role") MemberRole role);
}
