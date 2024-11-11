package com.authorizer.service;

import com.authorizer.chain.BalanceTypeStep;
import com.authorizer.domain.Account;
import com.authorizer.domain.Balance;
import com.authorizer.domain.BalanceEntity;
import com.authorizer.dto.TransactionDTO;
import com.authorizer.enums.BalanceTypeEnum;
import com.authorizer.exception.InsufficientBalanceException;
import com.authorizer.service.impl.AccountServiceImpl;
import com.authorizer.service.impl.BalanceServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public void processing(TransactionDTO transaction) throws Throwable {
        log.info("Starting transaction...");
        Account account = accountService.findById(transaction.getAccount());

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transaction)).findFirst();

        BalanceTypeEnum balanceType = first.get().getBalanceTypeEnum();
        log.info("Amount will be debited from the balance {}", balanceType);

        Optional<BalanceEntity> accountBalance = findBalanceByType(account, balanceType);

        BigDecimal amountTransaction = transaction.getTotalAmount();

        accountBalance.ifPresentOrElse(balanceEntity -> {
                    Balance balance = balanceEntity.toBalance();
                    /*if (balance.getAmount().compareTo(amountTransaction) >= 0) {
                        balance.setAmount(balance.getAmount().subtract(amountTransaction).setScale(2, RoundingMode.FLOOR));*/
                        balance.doDebit(amountTransaction);
                        balanceService.save(balance.toEntity());
                   /* } else {
                        Optional<BalanceEntity> balanceCash = findBalanceByType(account, BalanceTypeEnum.CASH);
                        balanceCash.ifPresent(cash -> {
                            if (cash.getAmount().compareTo(amountTransaction) >= 0) {
                                cash.setAmount(balanceEntity.getAmount().subtract(amountTransaction).setScale(2, RoundingMode.FLOOR));
                                balanceServiceImpl.save(cash);

                            } else {
                                throw new InsufficientBalanceException("Insufficient balance");
                            }

                        });
                    }*/
                    log.info("Authorization done...");

                },

                () -> {
                    log.info("Nothing to do...");
        });

    }

    private static Optional<BalanceEntity> findBalanceByType(Account account, BalanceTypeEnum balanceType) {
        return account.getBalanceEntities()
                .stream()
                .filter(balance -> balanceType.equals(balance.getType()))
                .findFirst();
    }


}




