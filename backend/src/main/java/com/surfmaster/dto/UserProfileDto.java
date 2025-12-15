package com.surfmaster.dto;

import com.surfmaster.entities.BoardType;
import com.surfmaster.entities.UserLevel;

import java.util.List;

public record UserProfileDto(
        Long id,
        String displayName,
        UserLevel level,
        List<BoardType> preferredBoards
) {
}
