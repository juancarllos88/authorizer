package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.Balance;
import com.authorizer.infrastructure.entity.BalanceEntity;
import com.authorizer.infrastructure.repository.BalanceRepository;
import com.authorizer.domain.services.BalanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
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

    @Override
    @Transactional
    public void updateBalanceAmount(Balance balance) {
         log.info("id {} updateBalanceAmount {}", balance.getId(),balance.getAmount());
        getRepository().updateBalanceAmount(balance.getId(), balance.getAmount());
    }
}
