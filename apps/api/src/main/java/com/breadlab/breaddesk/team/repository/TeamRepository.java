package com.breadlab.breaddesk.team.repository;

import com.breadlab.breaddesk.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    List<Team> findByActiveTrue();
    
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Team findByIdWithMembers(Long id);
}
