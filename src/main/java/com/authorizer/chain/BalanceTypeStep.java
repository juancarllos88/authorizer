package com.authorizer.chain;

import com.authorizer.dto.TransactionDTO;
import com.authorizer.enums.BalanceTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BalanceTypeStep {
    protected BalanceTypeEnum balanceTypeEnum;
    public abstract Integer getPriority();
    public abstract boolean searchInfo(TransactionDTO transaction);


}
