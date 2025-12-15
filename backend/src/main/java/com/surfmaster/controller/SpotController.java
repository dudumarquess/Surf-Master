package com.surfmaster.controller;

import com.surfmaster.dto.SpotDto;
import com.surfmaster.service.SpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {
    private final SpotService spotService;

    @GetMapping
    public List<SpotDto> getListOfSpots(){
        return spotService.listAll();
    }

    @GetMapping("/{id}")
    public SpotDto getSpotById(@PathVariable Long id){
        return spotService.getById(id);
    }
}
