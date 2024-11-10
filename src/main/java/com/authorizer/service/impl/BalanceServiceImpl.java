package com.authorizer.service.impl;

import com.authorizer.domain.Balance;
import com.authorizer.repository.BalanceRepository;
import com.authorizer.service.BalanceService;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl extends BaseServiceImpl<Balance> implements BalanceService {

    private final BalanceRepository balanceRepository;

    public BalanceServiceImpl(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    protected BalanceRepository getRepository() {
        return balanceRepository;
    }
}
