package com.authorizer.infrastructure.entity;


import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.model.Balance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
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
