package com.surfmaster.controller;

import com.surfmaster.dto.SurfSummaryDto;
import com.surfmaster.service.SurfSummaryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/summaries")
public class SurfSummaryController {
    private final SurfSummaryService surfSummaryService;

    @GetMapping("/spot/{spotId}/today")
    public SurfSummaryDto getTodaySummary(@PathVariable Long spotId) {
        return surfSummaryService.getOrGenerateToday(spotId);
    }
}
