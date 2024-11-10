package com.authorizer.chain;

import com.authorizer.dto.TransactionDTO;
import com.authorizer.enums.BalanceTypeEnum;
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
