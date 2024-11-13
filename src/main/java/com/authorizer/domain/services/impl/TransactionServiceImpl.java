package com.authorizer.domain.services.impl;

import com.authorizer.domain.chain.steps.BalanceTypeStep;
import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.exception.BalanceTypeNotFoundException;
import com.authorizer.domain.exception.InsufficientBalanceException;
import com.authorizer.domain.model.Account;
import com.authorizer.domain.model.Balance;
import com.authorizer.domain.model.Transaction;
import com.authorizer.domain.services.AccountService;
import com.authorizer.domain.services.BalanceService;
import com.authorizer.domain.services.TransactionService;
import com.authorizer.infrastructure.entity.TransactionEntity;
import com.authorizer.infrastructure.repository.TransactionRepository;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
public class TransactionServiceImpl extends BaseServiceImpl<Transaction, TransactionEntity> implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final BalanceService balanceService;
    private final List<BalanceTypeStep> stepsToFindBalanceType;
    private final Gson gson;

    public TransactionServiceImpl(TransactionRepository transactionRepository, List<BalanceTypeStep> stepsToFindBalanceType, AccountService accountService, BalanceService balanceService, Gson gson) {
        this.transactionRepository = transactionRepository;
        this.stepsToFindBalanceType = stepsToFindBalanceType;
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.gson = gson;
    }

    @Override
    protected TransactionRepository getRepository() {
        return transactionRepository;
    }

    public void authorization(TransactionDTO transaction) {
        log.info("Starting transaction...");

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transaction)).findFirst();

        BalanceTypeEnum balanceType = first.get().getBalanceTypeEnum();
        log.info("The transaction amount will use {} balance ", balanceType);

        Account account = accountService.findById(transaction.getAccount());
        findBalanceByType(account, balanceType).ifPresent(balance -> {
            try {
                log.info("The balance amount is {}", balance.getAmount());
                balance.doDebit(transaction.getTotalAmount());
                balanceService.save(balance);
            }catch (InsufficientBalanceException e) {
                log.info(e.getMessage());
                fallbackAuthorization(account,transaction.getTotalAmount());
            }
            log.info("Authorization done...");
            Transaction txn = new Transaction(null,gson.toJson(transaction), LocalDateTime.now(),transaction.getTotalAmount(),account.getId(),balance.getId());
            save(txn);
        });

    }

    private  Optional<Balance> findBalanceByType(Account account, BalanceTypeEnum balanceType) {
        return account.getBalances()
                .stream()
                .filter(balance -> balanceType.equals(balance.getType()))
                .findFirst();
    }

    private void fallbackAuthorization(Account account, BigDecimal transactionAmount) {
        log.info("The authorizer will attempt in the CASH balance of account {}", account.getId().toString());
        findBalanceByType(account, BalanceTypeEnum.CASH).ifPresentOrElse(balance -> {
            balance.doDebit(transactionAmount);
            balanceService.save(balance);
        }, () -> {
            throw new BalanceTypeNotFoundException(String.format("CASH balance not found to account %s ", account.getId().toString()));
        });
    }



}




