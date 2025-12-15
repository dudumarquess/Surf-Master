package com.surfmaster.service;

import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.entities.BoardType;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.entities.UserProfile;
import com.surfmaster.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void listProfilesMapsEntityFields() {
        var entity = UserProfile.builder()
                .id(1L)
                .displayName("Lia")
                .level(UserLevel.ADVANCED)
                .preferredBoards(new ArrayList<>(List.of(BoardType.LONGBOARD)))
                .build();
        when(userProfileRepository.findAll()).thenReturn(List.of(entity));

        List<UserProfileDto> dtos = userProfileService.listProfiles();

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).displayName()).isEqualTo("Lia");
        assertThat(dtos.get(0).preferredBoards()).containsExactly(BoardType.LONGBOARD);
    }

    @Test
    void createProfileNormalizesBoards() {
        var requestBoards = new ArrayList<>(List.of(BoardType.BODYBOARD));
        var request = new UpsertUserProfileRequest("Nina", UserLevel.BEGINNER, requestBoards);
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
            UserProfile entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        UserProfileDto dto = userProfileService.createProfile(request);

        assertThat(dto.id()).isEqualTo(10L);
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getPreferredBoards()).containsExactly(BoardType.BODYBOARD);
        assertThat(captor.getValue().getPreferredBoards()).isNotSameAs(requestBoards);
    }

    @Test
    void updateProfileOverwritesExistingValues() {
        var existing = UserProfile.builder()
                .id(5L)
                .displayName("Old")
                .level(UserLevel.BEGINNER)
                .preferredBoards(new ArrayList<>())
                .build();
        when(userProfileRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenReturn(existing);

        var request = new UpsertUserProfileRequest("Novo", UserLevel.INTERMEDIATE, List.of(BoardType.SHORTBOARD));

        UserProfileDto dto = userProfileService.updateProfile(5L, request);

        assertThat(dto.displayName()).isEqualTo("Novo");
        assertThat(existing.getPreferredBoards()).containsExactly(BoardType.SHORTBOARD);
    }

    @Test
    void deleteProfileDelegatesToRepository() {
        userProfileService.deleteProfile(4L);
        verify(userProfileRepository).deleteById(4L);
    }
}
