package com.breadlab.breaddesk.team.repository;

import com.breadlab.breaddesk.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByActiveTrue();

    Optional<Team> findByName(String name);

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);
}
