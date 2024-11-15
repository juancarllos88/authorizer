package com.authorizer.domain.model;


import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.exception.InsufficientBalanceException;
import com.authorizer.infrastructure.entity.BalanceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Balance extends BaseModel<UUID, BalanceEntity> {
    private BalanceTypeEnum type;
    private BigDecimal amount;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
    private Account account;

    public Balance(UUID id, BalanceTypeEnum type, BigDecimal amount, LocalDateTime insertedAt, LocalDateTime updatedAt, Account account) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.insertedAt = insertedAt;
        this.updatedAt = updatedAt;
        this.account = account;
    }

    @Override
    public BalanceEntity toEntity() {
        return new BalanceEntity(id, type, amount, insertedAt, updatedAt, account.toEntity());
    }

    public BigDecimal updateAmount(BigDecimal cachedBalanceAmount, BigDecimal amountTransaction) {
        if (cachedBalanceAmount.compareTo(amountTransaction) >= 0) {
            amount = cachedBalanceAmount.subtract(amountTransaction).setScale(2, RoundingMode.FLOOR);
            return amount;
        } else {
            throw new InsufficientBalanceException(String.format("insufficient balance id %s type %s by account %s",
                    id.toString(), type.toString(), account.getId().toString()));
        }
    }

}
