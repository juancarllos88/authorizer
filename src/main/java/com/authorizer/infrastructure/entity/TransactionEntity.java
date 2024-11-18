package com.authorizer.infrastructure.entity;

import com.authorizer.domain.model.Transaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class TransactionEntity extends BaseEntity<UUID, Transaction> {


    @NotNull
    @Column(name = "payload")
    private String payload;

    @NotNull
    @Column(name = "inserted_at")
    private LocalDateTime insertedAt;

    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;

    @NotNull
    @Column(name = "balance_id")
    private UUID balanceId;

    @NotNull
    @Column(name = "account_id")
    private UUID accountId;

    public TransactionEntity(UUID id, String payload, LocalDateTime insertedAt, BigDecimal amount, UUID balanceId, UUID accountId) {
        this.id = id;
        this.payload = payload;
        this.insertedAt = insertedAt;
        this.amount = amount;
        this.balanceId = balanceId;
        this.accountId = accountId;
    }


    @Override
    public Transaction toModel() {
        return new Transaction(id, payload, insertedAt, amount, balanceId, accountId);
    }
}
