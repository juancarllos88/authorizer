package com.authorizer.domain.services;

import com.authorizer.domain.chain.steps.BalanceTypeStep;
import com.authorizer.domain.model.Account;
import com.authorizer.domain.exception.InsufficientBalanceException;
import com.authorizer.domain.model.Balance;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.enums.BalanceTypeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
public class TransactionService {


    private final AccountService accountService;
    private final BalanceService balanceService;
    private final List<BalanceTypeStep> stepsToFindBalanceType;

    public TransactionService(List<BalanceTypeStep> stepsToFindBalanceType,  AccountService accountService, BalanceService balanceService) {
        this.stepsToFindBalanceType = stepsToFindBalanceType;
        this.accountService = accountService;
        this.balanceService = balanceService;
    }

    public void authorization(TransactionDTO transaction) throws Throwable {
        log.info("Starting transaction...");

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transaction)).findFirst();

        BalanceTypeEnum balanceType = first.get().getBalanceTypeEnum();
        log.info("Amount will be debited from the balance {}", balanceType);

        Account account = accountService.findById(transaction.getAccount());
        findBalanceByType(account, balanceType).ifPresent(balance -> {
            try {
                balance.doDebit(transaction.getTotalAmount());
                balanceService.save(balance);
            }catch (InsufficientBalanceException e) {
                fallbackAuthorization(account,transaction.getTotalAmount());
            }
            log.info("Authorization done...");
        });

    }

    private  Optional<Balance> findBalanceByType(Account account, BalanceTypeEnum balanceType) {
        return account.getBalances()
                .stream()
                .filter(balance -> balanceType.equals(balance.getType()))
                .findFirst();
    }

    private void fallbackAuthorization(Account account, BigDecimal transactionAmount) {
        findBalanceByType(account, BalanceTypeEnum.CASH).ifPresent(balance -> {
            balance.doDebit(transactionAmount);
            balanceService.save(balance);
        });
    }




}




