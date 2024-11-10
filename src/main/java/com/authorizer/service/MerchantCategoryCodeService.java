package com.authorizer.service;

import com.authorizer.domain.MerchantCategoryCode;

import java.util.Optional;

public interface MerchantCategoryCodeService extends BaseService<MerchantCategoryCode> {
    Optional<MerchantCategoryCode> findByCode(String mcc);
}
