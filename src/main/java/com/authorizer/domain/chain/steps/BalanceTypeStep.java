package com.authorizer.domain.chain.steps;

import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.enums.BalanceTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BalanceTypeStep {
    protected BalanceTypeEnum balanceTypeEnum;
    public abstract Integer getPriority();
    public abstract boolean searchInfo(TransactionDTO transaction);


}
