package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.Merchant;
import com.authorizer.infrastructure.entity.MerchantEntity;
import com.authorizer.infrastructure.repository.MerchantRepository;
import com.authorizer.domain.services.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MerchantServiceImpl extends BaseServiceImpl<Merchant, MerchantEntity> implements MerchantService {

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
        Optional<MerchantEntity> merchantEntity = getRepository().findByName(name);
        return merchantEntity.map(MerchantEntity::toModel);
    }

}
