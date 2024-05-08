package com.SafeNet.Backend.domain.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {
    private String sender;
    private String roomId;
    private String message;
    private String sentTime;
}
