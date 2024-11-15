package com.authorizer.infrastructure.entity;

import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.model.MerchantCategoryCode;
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
@Table(name = "merchants_category_code")
public class MerchantCategoryCodeEntity extends BaseEntity<UUID, MerchantCategoryCode> {

    private String code;

    @NotNull
    @Column(name = "balance_type")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum balanceType;

    public MerchantCategoryCodeEntity(UUID id, String code, BalanceTypeEnum balanceType) {
        this.id = id;
        this.code = code;
        this.balanceType = balanceType;
    }

    @Override
    public MerchantCategoryCode toModel() {
        return new MerchantCategoryCode(id, code, balanceType);
    }
}
