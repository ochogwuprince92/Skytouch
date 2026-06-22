package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.security.JwtTokenService;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String createSession(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public UUID resolveUserId(String sessionToken) {
        return jwtTokenService.resolveUserId(sessionToken);
    }
}
