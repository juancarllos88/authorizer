package com.authorizer.service;

import com.authorizer.domain.Merchant;

import java.util.Optional;

public interface MerchantService extends BaseService<Merchant> {
    Optional<Merchant> findByName(String name);

}
