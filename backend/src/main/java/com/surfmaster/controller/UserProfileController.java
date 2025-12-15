package com.surfmaster.controller;

import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.service.UserProfileService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public List<UserProfileDto> listProfiles() {
        return userProfileService.listProfiles();
    }

    @GetMapping("/{id}")
    public UserProfileDto getProfile(@PathVariable Long id) {
        return userProfileService.getProfile(id);
    }

    @PostMapping
    public UserProfileDto createProfile(@RequestBody UpsertUserProfileRequest request) {
        return userProfileService.createProfile(request);
    }

    @PutMapping("/{id}")
    public UserProfileDto updateProfile(@PathVariable Long id, @RequestBody UpsertUserProfileRequest request) {
        return userProfileService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProfile(@PathVariable Long id) {
        userProfileService.deleteProfile(id);
    }
}
