package com.SafeNet.Backend.domain.region.repository;

import com.SafeNet.Backend.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByCity(String city);
    Optional<Region> findByCityAndCountyAndDistrict(String city, String county, String district);
}

