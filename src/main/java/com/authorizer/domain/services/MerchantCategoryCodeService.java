package com.authorizer.domain.services;

import com.authorizer.domain.model.MerchantCategoryCode;

import java.util.Optional;

public interface MerchantCategoryCodeService extends BaseService<MerchantCategoryCode> {
    Optional<MerchantCategoryCode> findByCode(String mcc);
}
