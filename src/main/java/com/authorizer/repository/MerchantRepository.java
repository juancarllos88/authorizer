package com.authorizer.repository;

import com.authorizer.domain.Merchant;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends BaseRepository<Merchant, UUID> {
    Optional<Merchant> findByName(String name);
}
