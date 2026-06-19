package com.backend.Skytouch.authentication.repository;

import com.backend.Skytouch.user.entity.Users;

import java.util.Optional;

public interface AuthenticationRepository {

    Optional<Users> findByEmail(String email);

    Users save(Users user);
}
