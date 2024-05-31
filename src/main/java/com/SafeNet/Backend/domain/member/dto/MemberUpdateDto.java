package com.SafeNet.Backend.domain.member.dto;

import com.SafeNet.Backend.domain.region.entity.Region;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateDto {
    private String name;
    private String phoneNumber;
    private String password;
    private String address;
}
