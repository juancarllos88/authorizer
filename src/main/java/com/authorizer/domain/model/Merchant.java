package com.authorizer.domain.model;

import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.infrastructure.entity.MerchantEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Merchant extends BaseModel<UUID, MerchantEntity> {

    private String name;

    private BalanceTypeEnum balanceType;

    public Merchant(UUID id, String name, BalanceTypeEnum balanceType) {
        this.id = id;
        this.name = name;
        this.balanceType = balanceType;
    }

    @Override
    public MerchantEntity toEntity() {
        return new MerchantEntity(id, name, balanceType);
    }
}
