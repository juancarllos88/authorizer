package com.authorizer.domain;


import com.authorizer.enums.BalanceTypeEnum;
import com.authorizer.exception.InsufficientBalanceException;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance implements Serializable {
    private UUID id;
    private BalanceTypeEnum type;
    private BigDecimal amount;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
    private Account account;


    public BalanceEntity toEntity() {
        return new BalanceEntity(id, type, amount, insertedAt, updatedAt, account);
    }

    public void doDebit(BigDecimal amountTransaction) {
        if (amount.compareTo(amountTransaction) >= 0) {
            amount = amount.subtract(amountTransaction).setScale(2, RoundingMode.FLOOR);
        } else {
            throw new InsufficientBalanceException("insufficient balance");
        }
    }


}
