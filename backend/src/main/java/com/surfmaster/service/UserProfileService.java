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
 * Camada responsável por orquestrar operações relacionadas a {@link UserProfile}.
 * Contém validações e conversões entre entidades e DTOs.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    /**
     * Retorna todos os perfis cadastrados.
     *
     * @return lista de {@link UserProfileDto}
     */
    public List<UserProfileDto> listProfiles() {
        return userProfileRepository.findAll()
                .stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    /**
     * Recupera um perfil específico.
     *
     * @param id identificador do perfil
     * @return DTO do perfil
     * @throws java.util.NoSuchElementException caso não exista
     */
    public UserProfileDto getProfile(Long id) {
        var entity = userProfileRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return EntityMapper.toDto(entity);
    }

    /**
     * Cria um novo perfil a partir de um request validado.
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
     * Atualiza um perfil existente com os dados do request.
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
     * Remove um perfil existente.
     */
    public void deleteProfile(Long id) {
        userProfileRepository.deleteById(id);
    }

    /**
     * Garante imutabilidade e evita referências nulas na lista de pranchas preferidas.
     */
    private List<BoardType> normalizeBoards(List<BoardType> boards) {
        return boards == null ? new ArrayList<>() : new ArrayList<>(boards);
    }
}
