package com.authorizer.domain.chain.steps;

import com.authorizer.domain.model.MerchantCategoryCode;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.services.MerchantCategoryCodeService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import javax.persistence.Cacheable;
import java.util.Optional;

@Component
public class MerchantCodeStep extends BalanceTypeStep {

    private final MerchantCategoryCodeService merchantCategoryCodeService;

    public MerchantCodeStep(MerchantCategoryCodeService merchantCategoryCodeService) {
        this.merchantCategoryCodeService = merchantCategoryCodeService;
    }

    @Override
    public Integer getPriority() {
        return 2;
    }

    @Override

    public boolean searchInfo(TransactionDTO transaction) {
        Optional<MerchantCategoryCode> merchantCategoryCode = merchantCategoryCodeService.findByCode(transaction.getMcc());
        if(merchantCategoryCode.isPresent()) {
            setBalanceTypeEnum(merchantCategoryCode.get().getBalanceType());
            return true;
        }
        return false;
    }

}
