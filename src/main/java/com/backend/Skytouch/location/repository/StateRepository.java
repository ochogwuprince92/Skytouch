package com.backend.Skytouch.location.repository;

import com.backend.Skytouch.location.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {

    List<State> findByCountry_IdOrderByNameAsc(Integer countryId);
}
