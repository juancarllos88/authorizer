package com.authorizer.service.impl;

import com.authorizer.domain.Merchant;
import com.authorizer.repository.MerchantRepository;
import com.authorizer.service.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MerchantServiceImpl extends BaseServiceImpl<Merchant> implements MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    protected MerchantRepository getRepository() {
        return merchantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Merchant> findByName(String name) {
        return getRepository().findByName(name);
    }

}
