package com.authorizer.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseDTO(String code, UUID control,LocalDateTime creationDate, String message, List<String> errors) {
    public ResponseDTO(String code) {
        this(code,null,null,null,null);

    }
    public ResponseDTO(List<String> errors) {
        this(null,null,null,null,errors);

    }
}
