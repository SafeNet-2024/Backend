package com.SafeNet.Backend.domain.region.repository;

import com.SafeNet.Backend.domain.region.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCity(String city);
}

