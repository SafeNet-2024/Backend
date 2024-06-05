package com.SafeNet.Backend.domain.region;

import com.SafeNet.Backend.domain.region.entity.Region;

public class RegionParser {

    public static Region parseRegion(String address) {
        String[] parts = address.split(" ");

        if (parts.length < 3) {
            throw new IllegalArgumentException("Address must be in the format 'City County District'");
        }

        String city = parts[0];
        String county = parts[1];
        String district = parts[2];

        return Region.builder()
                .city(city)
                .county(county)
                .district(district)
                .build();
    }
}
