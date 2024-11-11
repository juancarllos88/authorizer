package com.authorizer.domain.model;

import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.infrastructure.entity.MerchantEntity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
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
