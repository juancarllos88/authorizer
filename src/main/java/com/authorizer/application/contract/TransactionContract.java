package com.authorizer.application.contract;

import com.authorizer.presentation.dto.transaction.TransactionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@Tag(name = "1. Transactions", description = "Transact with the card")
@RequestMapping(value = "/transactions")
public interface TransactionContract {

    @Operation(summary = "Make a transaction", security = {@SecurityRequirement(name = "idempotencyToken")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class)))
    })
    @PostMapping("/authorization")
    public ResponseEntity<TransactionResponseDTO> authorization(@RequestBody @Valid com.authorizer.presentation.dto.transaction.TransactionDTO transaction) throws Throwable;

}
