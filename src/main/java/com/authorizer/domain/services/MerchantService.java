package com.authorizer.domain.services;

import com.authorizer.domain.model.Merchant;

import java.util.Optional;

public interface MerchantService extends BaseService<Merchant> {
    Optional<Merchant> findByName(String name);

}
