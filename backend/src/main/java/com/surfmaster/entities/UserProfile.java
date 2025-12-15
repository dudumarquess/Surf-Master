
package com.surfmaster.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opcional: nome “público” que o user preferir */
    @Column(length = 120)
    private String displayName;

    /** Nível de surf sugerido para personalização */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserLevel level;


    @Builder.Default
    @ElementCollection(targetClass = BoardType.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_profile_boards",
            joinColumns = @JoinColumn(name = "user_profile_id")
    )
    @Column(name = "board")
    @Enumerated(EnumType.STRING)
    private List<BoardType> preferredBoards = new ArrayList<>();

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
