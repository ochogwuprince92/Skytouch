package com.backend.Skytouch.savedjob.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.savedjob.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public PageResponse<JobResponse> getMySavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return savedJobService.findMySavedJobs(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }

    @PostMapping("/jobs/{jobId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public void saveJob(@PathVariable java.util.UUID jobId) {
        savedJobService.save(SecurityUtils.getCurrentUser().getEmail(), jobId);
    }

    @DeleteMapping("/jobs/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public void unsaveJob(@PathVariable java.util.UUID jobId) {
        savedJobService.unsave(SecurityUtils.getCurrentUser().getEmail(), jobId);
    }
}
