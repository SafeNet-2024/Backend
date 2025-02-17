    package com.SafeNet.Backend.domain.message.dto;

    import com.fasterxml.jackson.annotation.JsonInclude;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class MessageRequestDto {
        private String receiver;
        private Long postId;
    }