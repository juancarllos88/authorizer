package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.MerchantCategoryCode;
import com.authorizer.infrastructure.entity.MerchantCategoryCodeEntity;
import com.authorizer.infrastructure.repository.MerchantCategoryCodeRepository;
import com.authorizer.domain.services.MerchantCategoryCodeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MerchantCategoryCodeServiceImpl extends BaseServiceImpl<MerchantCategoryCode, MerchantCategoryCodeEntity> implements MerchantCategoryCodeService {

    private final MerchantCategoryCodeRepository merchantCategoryCodeRepository;

    public MerchantCategoryCodeServiceImpl(MerchantCategoryCodeRepository merchantCategoryCodeRepository) {
        this.merchantCategoryCodeRepository = merchantCategoryCodeRepository;
    }

    @Override
    protected MerchantCategoryCodeRepository getRepository() {
        return merchantCategoryCodeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("merchant_findByCode_mcc")
    public Optional<MerchantCategoryCode> findByCode(String mcc) {
        Optional<MerchantCategoryCodeEntity> merchantCategoryCodeEntity = getRepository().findByCode(mcc);
        return merchantCategoryCodeEntity.map(MerchantCategoryCodeEntity::toModel);
    }


}
