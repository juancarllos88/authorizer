package com.authorizer.domain.services.impl;

import com.authorizer.domain.chain.steps.BalanceTypeStep;
import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.exception.EntityNotFoundException;
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
    private final RedisTemplate<String, ConcurrentCacheControl> redisTemplate;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  List<BalanceTypeStep> stepsToFindBalanceType,
                                  AccountService accountService,
                                  BalanceService balanceService,
                                  RedisTemplate<String, ConcurrentCacheControl> redisTemplate) {
        this.transactionRepository = transactionRepository;
        this.stepsToFindBalanceType = stepsToFindBalanceType;
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.redisTemplate = redisTemplate;
    }



    @Override
    protected TransactionRepository getRepository() {
        return transactionRepository;
    }

    private void calculateCacheBalanceAmount(UUID accountID, Balance balance, BigDecimal transactionAmount) {
        String accountFromRequest = accountID.toString();

        String balanceAmountCacheKey = "KEY_BALANCE_" + balance.getType().toString() + "_ACCOUNT_" + accountFromRequest;
        BoundValueOperations<String, ConcurrentCacheControl> keyBalanceAmountOperation = redisTemplate.boundValueOps(balanceAmountCacheKey);
        boolean isAbsent = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(balanceAmountCacheKey,
                ConcurrentCacheControl.initBalance(balance.getAmount()), 60, TimeUnit.MINUTES));

        if (isAbsent) {
            log.info("cache {} not exist ", balanceAmountCacheKey);
            updateBalanceInCache(balance, balance.getAmount(), transactionAmount, balanceAmountCacheKey, keyBalanceAmountOperation);

        } else {
            synchronized (balanceAmountCacheKey) {
                ConcurrentCacheControl balanceCached = null;
                do{
                    balanceCached = keyBalanceAmountOperation.get();
                }while (!balanceCached.isDone());
                log.info("balance amount {} exists in cache {}", balanceCached.getBalanceAmount(), balanceAmountCacheKey);
                updateBalanceInCache(balance, balanceCached.getBalanceAmount(), transactionAmount, balanceAmountCacheKey, keyBalanceAmountOperation);
            }
        }

    }

    private void updateBalanceInCache(Balance balance, BigDecimal cachedBalanceAmount,  BigDecimal transactionAmount, String balanceAmountCacheKey,
                                             BoundValueOperations<String, ConcurrentCacheControl> keyBalanceAmountOperation) {

        BigDecimal balanceToBeCached = null;
        try{
            balanceToBeCached = balance.updateAmount(cachedBalanceAmount,transactionAmount);

        }catch (InsufficientBalanceException e){
            ConcurrentCacheControl cachedResponse = keyBalanceAmountOperation.get();
            cachedResponse.setDone(true);
            keyBalanceAmountOperation.set(cachedResponse, 60, TimeUnit.MINUTES);
            throw e;
        }

        ConcurrentCacheControl result = ConcurrentCacheControl.doneBalance(balanceToBeCached);
        //result.setDone(true);

        log.info("update new balance amount {} in cache {} ", balanceToBeCached, balanceAmountCacheKey);
        keyBalanceAmountOperation.set(result, 60, TimeUnit.MINUTES);
    }

    public AuthorizationStatusEnum authorization(TransactionDTO transactionDTO) {
        log.info("Starting transaction {} ...", transactionDTO.getId().toString());

        Account account = accountService.findById(transactionDTO.getAccount());

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transactionDTO)).findFirst();

        BalanceTypeEnum balanceType = first.get().getBalanceTypeEnum();
        log.info("The transaction amount will use {} balance ", balanceType);

        findBalanceByType(account, balanceType).ifPresent(balance -> {
            try {

                calculateCacheBalanceAmount(account.getId(), balance, transactionDTO.getTotalAmount());

                balanceService.updateBalanceAmount(balance);
            }catch (InsufficientBalanceException e) {
                log.info("A new attempt will be made to authorize the account {} using the cash balance.", transactionDTO.getAccount());
                log.error(e.getMessage());
                fallbackAuthorization(account,transactionDTO.getTotalAmount());
            }
            log.info("Authorization done...");
            Gson gson = new Gson();
            ///Transaction transaction = new Transaction(null,gson.toJson(transactionDTO), LocalDateTime.now(),transactionDTO.getTotalAmount(),account.getId(),balance.getId());
            Transaction transaction = Transaction.builder()
                    .payload(gson.toJson(transactionDTO))
                    .insertedAt(LocalDateTime.now())
                    .amount(transactionDTO.getTotalAmount())
                    .accountId(account.getId())
                    .balanceId(balance.getId())
                    .build();
            save(transaction);
        });

        return AuthorizationStatusEnum.APPROVED;

    }

    private Optional<Balance> findBalanceByType(Account account, BalanceTypeEnum balanceType) {
        return Optional.ofNullable(account.getBalances()
                .stream()
                .filter(balance -> balanceType.equals(balance.getType()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(String.format("%s balance not found to account %s ", balanceType, account.getId().toString()))));
    }

    /**
     * If the balance on the first attempt is not sufficient,
     * an attempt will be made on the cash balance,
     * if the account has this type of balance.
     * @param account cardholder account
     * @param transactionAmount transaction amount
     */
    private void fallbackAuthorization(Account account, BigDecimal transactionAmount) {
        log.info("The authorizer will attempt in the CASH balance of account {}", account.getId().toString());
        findBalanceByType(account, BalanceTypeEnum.CASH).ifPresentOrElse(balance -> {

            calculateCacheBalanceAmount(account.getId(), balance, transactionAmount);
            balanceService.updateBalanceAmount(balance);
        }, () -> {
            throw new EntityNotFoundException(String.format("CASH balance not found to account %s ", account.getId().toString()));
        });
    }



}




