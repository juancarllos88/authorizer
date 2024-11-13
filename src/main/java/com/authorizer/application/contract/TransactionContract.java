package com.authorizer.application.contract;

import com.authorizer.presentation.dto.response.ResponseDTO;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@Tag(name = "1. Transactions", description = "Transact with the card")
@RequestMapping(value = "/transactions")
public interface TransactionContract {

    @Operation(summary = "Make a transaction")
    @ApiResponses(value =  {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/authorization")
    public ResponseEntity<ResponseDTO> authorization(@RequestBody @Valid TransactionDTO transaction) throws Throwable;
}
