package com.surfmaster.service;

import com.surfmaster.dto.SpotDto;
import com.surfmaster.entities.Spot;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;

    @Transactional
    public List<SpotDto> listAll() {
        return spotRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    @Transactional
    public SpotDto getById(Long id) {
        Spot spot = spotRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Spot not found:" + id));
        return EntityMapper.toDto(spot);
    }
}
