package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.Balance;
import com.authorizer.infrastructure.entity.BalanceEntity;
import com.authorizer.infrastructure.repository.BalanceRepository;
import com.authorizer.domain.services.BalanceService;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl extends BaseServiceImpl<Balance, BalanceEntity> implements BalanceService {

    private final BalanceRepository balanceRepository;

    public BalanceServiceImpl(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    protected BalanceRepository getRepository() {
        return balanceRepository;
    }
}
