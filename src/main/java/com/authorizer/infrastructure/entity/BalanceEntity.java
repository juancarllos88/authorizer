package com.authorizer.infrastructure.entity;


import com.authorizer.domain.model.Balance;
import com.authorizer.domain.enums.BalanceTypeEnum;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "balances")
public class BalanceEntity extends BaseEntity<UUID,Balance> {

    @NotNull
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum type;

    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;

    @NotNull
    @Column(name = "inserted_at")
    private LocalDateTime insertedAt;

    @NotNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JoinColumn(name = "account_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AccountEntity account;

    public BalanceEntity(UUID id, BalanceTypeEnum type, BigDecimal amount, LocalDateTime insertedAt, LocalDateTime updatedAt, AccountEntity account) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.insertedAt = insertedAt;
        this.updatedAt = updatedAt;
        this.account = account;
    }

    @PreUpdate
    public void updatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Balance toModel() {
        return new Balance(id, type, amount, insertedAt, updatedAt, account.toModel());
    }
}
