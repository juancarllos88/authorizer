package com.authorizer.service.impl;

import com.authorizer.domain.MerchantCategoryCode;
import com.authorizer.repository.MerchantCategoryCodeRepository;
import com.authorizer.service.MerchantCategoryCodeService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantCategoryCodeServiceImpl extends BaseServiceImpl<MerchantCategoryCode> implements MerchantCategoryCodeService {

    private final MerchantCategoryCodeRepository merchantCategoryCodeRepository;

    public MerchantCategoryCodeServiceImpl(MerchantCategoryCodeRepository merchantCategoryCodeRepository) {
        this.merchantCategoryCodeRepository = merchantCategoryCodeRepository;
    }

    @Override
    protected MerchantCategoryCodeRepository getRepository() {
        return merchantCategoryCodeRepository;
    }

    @Override
    public Optional<MerchantCategoryCode> findByCode(String mcc) {
        return getRepository().findByCode(mcc);
    }
}
