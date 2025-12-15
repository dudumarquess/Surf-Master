package com.surfmaster.repository;

import com.surfmaster.entities.ChatSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    @EntityGraph(attributePaths = {"messages"})
    @Query("select s from ChatSession s where s.id = :id")
    Optional<ChatSession> findByIdWithMessages(Long id);
}
