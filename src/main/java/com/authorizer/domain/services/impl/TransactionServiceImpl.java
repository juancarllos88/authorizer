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
import com.authorizer.infrastructure.filter.ConcurrentCacheControl;
import com.authorizer.infrastructure.repository.TransactionRepository;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@Transactional
public class TransactionServiceImpl extends BaseServiceImpl<Transaction, TransactionEntity> implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final BalanceService balanceService;
    private final List<BalanceTypeStep> stepsToFindBalanceType;
    private final Gson gson;
    private final RedisTemplate<String, ConcurrentCacheControl> redisTemplate;

    public TransactionServiceImpl(TransactionRepository transactionRepository, List<BalanceTypeStep> stepsToFindBalanceType, AccountService accountService, BalanceService balanceService, Gson gson, RedisTemplate<String, ConcurrentCacheControl> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.stepsToFindBalanceType = stepsToFindBalanceType;
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.gson = gson;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected TransactionRepository getRepository() {
        return transactionRepository;
    }

    private void analyzeBalance(UUID accountID, Balance balance, BigDecimal transactionAmount) {
        String accountFromRequest = accountID.toString();

        String concurrencyAccountCacheKey = "KEY_BALANCE_" + balance.getType().toString() + "_ACCOUNT_" + accountFromRequest;
        BoundValueOperations<String, ConcurrentCacheControl> keyAccountOperation = redisTemplate.boundValueOps(concurrencyAccountCacheKey);
        boolean isAbsent = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(concurrencyAccountCacheKey,
                ConcurrentCacheControl.initBalance(balance.getAmount()), 60, TimeUnit.MINUTES));
        //boolean isAbsent = keyAccountOperation.ops setIfAbsent(ConcurrentCacheControl.initBalance(balance.getAmount()), 60, TimeUnit.MINUTES);
        if (isAbsent) {
            log.info("cache {} not exist ", concurrencyAccountCacheKey);
            updateBalanceInCache(balance, balance.getAmount(), transactionAmount, concurrencyAccountCacheKey, keyAccountOperation);

        } else {
            synchronized (this) {
                ConcurrentCacheControl balanceCached = null;
                do{
                    balanceCached = keyAccountOperation.get();
                }while (!balanceCached.isDone);
                log.info("balance amount {} exists in cache {}", balanceCached.balanceAmount, concurrencyAccountCacheKey);
                updateBalanceInCache(balance, balanceCached.balanceAmount, transactionAmount, concurrencyAccountCacheKey, keyAccountOperation);
            }
        }

    }

    private void updateBalanceInCache(Balance balance, BigDecimal cachedBalanceAmount,  BigDecimal transactionAmount, String concurrencyAccountCacheKey,
                                             BoundValueOperations<String, ConcurrentCacheControl> keyAccountOperation) {

        BigDecimal balanceToBeCached = balance.doDebit(cachedBalanceAmount,transactionAmount);

        ConcurrentCacheControl result = ConcurrentCacheControl.initBalance(balanceToBeCached);
        result.setDone(true);

        log.info("update new balance amount {} in cache {} ", balanceToBeCached, concurrencyAccountCacheKey);
        keyAccountOperation.set(result, 60, TimeUnit.MINUTES);
    }

    public void authorization(TransactionDTO transactionDTO) {
        log.info("Starting transaction {} ...", transactionDTO.getId().toString());

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transactionDTO)).findFirst();

        BalanceTypeEnum balanceType = first.get().getBalanceTypeEnum();
        log.info("The transactionDTO amount will use {} balance ", balanceType);

        Account account = accountService.findById(transactionDTO.getAccount());
        findBalanceByType(account, balanceType).ifPresent(balance -> {
            try {

                analyzeBalance(account.getId(), balance, transactionDTO.getTotalAmount());

                //balance.doDebit(transactionDTO.getTotalAmount());
                //balanceService.save(balance);
                balanceService.updateBalanceAmount(balance);
            }catch (InsufficientBalanceException e) {
                log.info(e.getMessage());
                fallbackAuthorization(account,transactionDTO.getTotalAmount());
            }
            log.info("Authorization done...");
            Transaction transaction = new Transaction(null,gson.toJson(transactionDTO), LocalDateTime.now(),transactionDTO.getTotalAmount(),account.getId(),balance.getId());
            save(transaction);
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




