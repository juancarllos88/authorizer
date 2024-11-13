package com.authorizer.infrastructure.entity;

import com.authorizer.domain.model.MerchantCategoryCode;
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
