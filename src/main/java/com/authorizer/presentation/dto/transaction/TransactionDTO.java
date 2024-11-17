package com.authorizer.presentation.dto.transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @Schema(description = "unique sequence to identify the transaction", defaultValue = "a8f0278-ba91-40c0-b924-1719859ddf3b")
    @NotNull
    public UUID id;
    @Schema(description = "cardholder account", defaultValue = "554e590b-a4bc-4859-b245-cbb4701fdbbd")
    @NotNull
    public UUID account;
    @Schema(description = "total transaction value", defaultValue = "1.00")
    @NotNull
    public BigDecimal totalAmount;
    @Schema(description = "merchant category code", defaultValue = "5811")
    @NotBlank
    public String mcc;
    @Schema(description = "merchant name", defaultValue = "PADARIA DO ZE               SAO PAULO BR")
    @NotBlank
    public String merchant;

}
