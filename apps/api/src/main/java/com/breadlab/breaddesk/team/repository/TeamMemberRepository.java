package com.breadlab.breaddesk.team.repository;

import com.breadlab.breaddesk.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    List<TeamMember> findByTeamId(Long teamId);
    
    List<TeamMember> findByMemberId(Long memberId);
    
    Optional<TeamMember> findByTeamIdAndMemberId(Long teamId, Long memberId);
    
    void deleteByTeamIdAndMemberId(Long teamId, Long memberId);
}
