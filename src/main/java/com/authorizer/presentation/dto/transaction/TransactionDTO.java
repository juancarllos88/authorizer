package com.authorizer.presentation.dto.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransactionDTO {
    public UUID id;
    public UUID account;
    public BigDecimal totalAmount;
    public String mcc;
    public String merchant;
}
