package com.breadlab.breaddesk.team.repository;

import com.breadlab.breaddesk.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByMemberId(Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberId(Long teamId, Long memberId);

    boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

    void deleteByTeamIdAndMemberId(Long teamId, Long memberId);

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.member WHERE tm.team.id = :teamId")
    List<TeamMember> findByTeamIdWithMember(@Param("teamId") Long teamId);
}
