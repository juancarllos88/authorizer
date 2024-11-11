package com.authorizer.domain.chain.steps;

import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.enums.BalanceTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class DefaultStep extends BalanceTypeStep {
    @Override
    public Integer getPriority() {
        return 3;
    }

    @Override
    public boolean searchInfo(TransactionDTO transaction) {
        setBalanceTypeEnum(BalanceTypeEnum.CASH);
        return true;
    }

}
