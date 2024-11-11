package com.authorizer.infrastructure.repository;

import com.authorizer.infrastructure.entity.MerchantEntity;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends BaseRepository<MerchantEntity, UUID> {
    Optional<MerchantEntity> findByName(String name);
}
