package com.breadlab.breaddesk.personal.repository;

import com.breadlab.breaddesk.personal.entity.PersonalNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalNoteRepository extends JpaRepository<PersonalNote, Long> {

    Page<PersonalNote> findByMemberId(Long memberId, Pageable pageable);
}
