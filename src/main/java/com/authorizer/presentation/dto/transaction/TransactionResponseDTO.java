package com.authorizer.presentation.dto.transaction;

import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponseDTO(
        @Schema(description = "authorization status: APPROVED | REJECTED | ERROR", allowableValues = {"00","51","07"})
        String code
) {

}
