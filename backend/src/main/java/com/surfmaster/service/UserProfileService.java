package com.surfmaster.service;

import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.entities.UserProfile;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.UserProfileRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.surfmaster.entities.BoardType;

import java.util.ArrayList;
import java.util.List;

/**
 * Layer responsible for orchestrating operations related to {@link UserProfile}.
 * Contains validations and conversions between entities and DTOs.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    /**
     * Returns every stored profile.
     *
     * @return list of {@link UserProfileDto}
     */
    public List<UserProfileDto> listProfiles() {
        return userProfileRepository.findAll()
                .stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    /**
     * Retrieves a specific profile.
     *
     * @param id profile identifier
     * @return profile DTO
     * @throws java.util.NoSuchElementException when it does not exist
     */
    public UserProfileDto getProfile(Long id) {
        var entity = userProfileRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return EntityMapper.toDto(entity);
    }

    /**
     * Creates a new profile from a validated request.
     */
    public UserProfileDto createProfile(UpsertUserProfileRequest request) {
        var entity = UserProfile.builder()
                .displayName(request.displayName())
                .level(request.level())
                .preferredBoards(normalizeBoards(request.preferredBoards()))
                .build();
        entity = userProfileRepository.save(entity);
        return EntityMapper.toDto(entity);
    }

    /**
     * Updates an existing profile with request data.
     */
    public UserProfileDto updateProfile(Long id, UpsertUserProfileRequest request) {
        var entity = userProfileRepository.findById(id).orElseThrow();
        entity.setDisplayName(request.displayName());
        entity.setLevel(request.level());
        entity.setPreferredBoards(normalizeBoards(request.preferredBoards()));
        entity = userProfileRepository.save(entity);
        return EntityMapper.toDto(entity);
    }

    /**
     * Removes an existing profile.
     */
    public void deleteProfile(Long id) {
        userProfileRepository.deleteById(id);
    }

    /**
     * Ensures immutability and avoids null references in the preferred board list.
     */
    private List<BoardType> normalizeBoards(List<BoardType> boards) {
        return boards == null ? new ArrayList<>() : new ArrayList<>(boards);
    }
}
