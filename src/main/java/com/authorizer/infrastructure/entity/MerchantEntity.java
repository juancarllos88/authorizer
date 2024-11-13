package com.authorizer.infrastructure.entity;

import com.authorizer.domain.model.Merchant;
import com.authorizer.domain.enums.BalanceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Builder
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
