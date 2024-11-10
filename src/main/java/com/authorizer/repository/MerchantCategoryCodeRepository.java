package com.authorizer.repository;

import com.authorizer.domain.MerchantCategoryCode;

import java.util.Optional;
import java.util.UUID;

public interface MerchantCategoryCodeRepository extends BaseRepository<MerchantCategoryCode, UUID> {
    Optional<MerchantCategoryCode> findByCode(String mcc);
}
