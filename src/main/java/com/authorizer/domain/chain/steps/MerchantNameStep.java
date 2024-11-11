package com.authorizer.domain.chain.steps;

import com.authorizer.domain.model.Merchant;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.services.MerchantService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MerchantNameStep extends BalanceTypeStep {

    private final MerchantService merchantService;

    public MerchantNameStep(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Override
    public Integer getPriority() {
        return 1;
    }

    @Override
    public boolean searchInfo(TransactionDTO transaction) {
        Optional<Merchant> merchant = merchantService.findByName(transaction.getMerchant());
        if(merchant.isPresent()) {
            setBalanceTypeEnum(merchant.get().getBalanceType());
            return true;
        }
        return false;
    }

}
