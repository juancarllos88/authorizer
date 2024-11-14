package com.authorizer.domain.services;

import com.authorizer.domain.model.Balance;

public interface BalanceService extends BaseService<Balance> {
    void updateBalanceAmount(Balance balance);
}
