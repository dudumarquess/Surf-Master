package com.surfmaster.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    private ChatRole chatRole;

    private String content;

    @CreationTimestamp
    private OffsetDateTime createdAt;

}
