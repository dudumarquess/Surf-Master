package com.surfmaster.service;

import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.entities.UserProfile;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.surfmaster.entities.BoardType;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public List<UserProfileDto> listProfiles() {
        return userProfileRepository.findAll()
                .stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    public UserProfileDto getProfile(Long id) {
        var entity = userProfileRepository.findById(id).orElseThrow();
        return EntityMapper.toDto(entity);
    }

    public UserProfileDto createProfile(UpsertUserProfileRequest request) {
        var entity = UserProfile.builder()
                .displayName(request.displayName())
                .level(request.level())
                .preferredBoards(normalizeBoards(request.preferredBoards()))
                .build();
        entity = userProfileRepository.save(entity);
        return EntityMapper.toDto(entity);
    }

    public UserProfileDto updateProfile(Long id, UpsertUserProfileRequest request) {
        var entity = userProfileRepository.findById(id).orElseThrow();
        entity.setDisplayName(request.displayName());
        entity.setLevel(request.level());
        entity.setPreferredBoards(normalizeBoards(request.preferredBoards()));
        entity = userProfileRepository.save(entity);
        return EntityMapper.toDto(entity);
    }

    public void deleteProfile(Long id) {
        userProfileRepository.deleteById(id);
    }

    private List<BoardType> normalizeBoards(List<BoardType> boards) {
        return boards == null ? new ArrayList<>() : new ArrayList<>(boards);
    }
}
