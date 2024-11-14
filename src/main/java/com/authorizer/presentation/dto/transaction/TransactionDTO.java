package com.authorizer.presentation.dto.transaction;

import com.authorizer.domain.model.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionDTO {
    @NotNull
    public UUID id;
    @NotNull
    public UUID account;
    @NotNull
    public BigDecimal totalAmount;
    @NotBlank
    public String mcc;
    @NotBlank
    public String merchant;

}
