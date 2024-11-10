package com.authorizer.chain;

import com.authorizer.domain.MerchantCategoryCode;
import com.authorizer.dto.TransactionDTO;
import com.authorizer.service.MerchantCategoryCodeService;
import org.springframework.stereotype.Component;

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
