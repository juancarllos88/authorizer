package com.authorizer.domain.model;

import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.infrastructure.entity.MerchantCategoryCodeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategoryCode extends BaseModel<UUID, MerchantCategoryCodeEntity> {


    private String code;

    private BalanceTypeEnum balanceType;

    public MerchantCategoryCode(UUID id, String code, BalanceTypeEnum balanceType) {
        this.id = id;
        this.code = code;
        this.balanceType = balanceType;
    }

    @Override
    public MerchantCategoryCodeEntity toEntity() {
        return new MerchantCategoryCodeEntity(id, code, balanceType);
    }
}
