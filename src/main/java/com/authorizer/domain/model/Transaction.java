package com.authorizer.domain.model;

import com.authorizer.infrastructure.entity.TransactionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseModel<UUID, TransactionEntity> {

    private String payload;
    private LocalDateTime insertedAt;
    private BigDecimal amount;
    private UUID balanceId;
    private UUID accountId;

    public Transaction(UUID id, @NotNull String payload, @NotNull LocalDateTime insertedAt, @NotNull BigDecimal amount, @NotNull UUID balanceId, @NotNull UUID accountId) {
        this.id = id;
        this.payload = payload;
        this.insertedAt = insertedAt;
        this.amount = amount;
        this.balanceId = balanceId;
        this.accountId = accountId;
    }

    @Override
    public TransactionEntity toEntity() {
        return new TransactionEntity(id, payload, insertedAt, amount, balanceId, accountId);
    }
}
