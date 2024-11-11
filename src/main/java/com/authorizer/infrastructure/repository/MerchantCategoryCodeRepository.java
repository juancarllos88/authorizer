package com.authorizer.infrastructure.repository;

import com.authorizer.infrastructure.entity.MerchantCategoryCodeEntity;

import java.util.Optional;
import java.util.UUID;

public interface MerchantCategoryCodeRepository extends BaseRepository<MerchantCategoryCodeEntity, UUID> {
    Optional<MerchantCategoryCodeEntity> findByCode(String mcc);
}
