package com.authorizer.infrastructure.entity;

import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.model.Merchant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "merchants")
public class MerchantEntity extends BaseEntity<UUID, Merchant> {

    private String name;

    @NotNull
    @Column(name = "balance_type")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum balanceType;

    public MerchantEntity(UUID id, String name, BalanceTypeEnum balanceType) {
        this.id = id;
        this.name = name;
        this.balanceType = balanceType;
    }

    @Override
    public Merchant toModel() {
        return new Merchant(id, name, balanceType);
    }
}
