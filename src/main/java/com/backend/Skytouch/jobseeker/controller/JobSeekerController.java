package com.backend.Skytouch.jobseeker.controller;

import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerRequest;
import com.backend.Skytouch.jobseeker.service.JobSeekerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-seekers")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    @GetMapping
    public List<JobSeekerResponse> list() {
        return jobSeekerService.findAll();
    }

    @GetMapping("/{id}")
    public JobSeekerResponse getById(@PathVariable UUID id) {
        return jobSeekerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobSeekerResponse register(@RequestBody RegisterJobSeekerRequest request) {
        return jobSeekerService.register(request);
    }
}
