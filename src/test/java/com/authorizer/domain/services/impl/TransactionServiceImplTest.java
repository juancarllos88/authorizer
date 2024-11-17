package com.authorizer.domain.services.impl;

import com.authorizer.domain.chain.steps.BalanceTypeStep;
import com.authorizer.domain.chain.steps.MerchantNameStep;
import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.enums.BalanceTypeEnum;
import com.authorizer.domain.exception.EntityNotFoundException;
import com.authorizer.domain.exception.InsufficientBalanceException;
import com.authorizer.domain.model.Account;
import com.authorizer.domain.model.Balance;
import com.authorizer.domain.model.Merchant;
import com.authorizer.domain.model.Transaction;
import com.authorizer.domain.services.AccountService;
import com.authorizer.domain.services.BalanceService;
import com.authorizer.infrastructure.filter.ConcurrentCacheControl;
import com.authorizer.infrastructure.repository.TransactionRepository;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceImplTest {


    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private BalanceService balanceService;
    @Mock
    private List<BalanceTypeStep> stepsToFindBalanceType;
    @Mock
    private RedisTemplate<String, ConcurrentCacheControl> redisTemplate;
    @Mock
    private MerchantServiceImpl merchantService;

    private TransactionDTO transactionDTO;
    private Account account;
    private Balance balanceFood;
    private Balance balanceCash;
    BoundValueOperations<String, ConcurrentCacheControl> keyBalanceAmountOperation;

    @BeforeEach
    public void setup() {
        transactionService = new TransactionServiceImpl(transactionRepository,
                List.of(new MerchantNameStep(merchantService)),
                accountService,
                balanceService,
                redisTemplate);

        UUID accountId = UUID.randomUUID();
        transactionDTO = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .merchant("PADARIA")
                .account(accountId)
                .build();

        account = new Account();
        account.setId(accountId);

        balanceFood = new Balance();
        balanceFood.setId(UUID.randomUUID());
        balanceFood.setType(BalanceTypeEnum.FOOD);
        balanceFood.setAmount(new BigDecimal(1000));
        balanceFood.setAccount(account);
        balanceCash = new Balance();
        balanceCash.setId(UUID.randomUUID());
        balanceCash.setType(BalanceTypeEnum.CASH);
        balanceCash.setAmount(new BigDecimal(1000));
        balanceCash.setAccount(account);

        List<Balance> balances = new ArrayList<>();
        balances.add(balanceFood);
        balances.add(balanceCash);
        account.setBalances(balances);

        Optional<Merchant> merchant = Optional.of(new Merchant(UUID.randomUUID(), "PADARIA", BalanceTypeEnum.FOOD));

        when(accountService.findById(transactionDTO.getAccount())).thenReturn(account);
        when(merchantService.findByName(transactionDTO.getMerchant())).thenReturn(merchant);

        keyBalanceAmountOperation = Mockito.mock(BoundValueOperations.class);
        when(redisTemplate.boundValueOps(Mockito.anyString())).thenReturn(keyBalanceAmountOperation);
        ValueOperations<String, ConcurrentCacheControl> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);


    }

    @Test
    void authorizationWithSuccess() {

        transactionDTO.setTotalAmount(BigDecimal.TEN);

        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Gson gson = new Gson();
        Transaction transaction = Transaction.builder()
                .payload(gson.toJson(transactionDTO))
                .insertedAt(LocalDateTime.now())
                .amount(transactionDTO.getTotalAmount())
                .accountId(account.getId())
                .balanceId(balanceFood.getId())
                .build();
        when(transactionRepository.save(transaction.toEntity())).thenReturn(transaction.toEntity());

        AuthorizationStatusEnum sut = transactionService.authorization(transactionDTO);

        assertThat(sut).isEqualTo(AuthorizationStatusEnum.APPROVED);
        verify(keyBalanceAmountOperation,times(1)).set(Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));

    }


    @Test
    void authorizationThrowsExceptionCauseInsufficientBalance() {

        transactionDTO.setTotalAmount(new BigDecimal(10000));
        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.init());

        assertThatThrownBy(() -> transactionService.authorization(transactionDTO)).isInstanceOf(InsufficientBalanceException.class);
        verify(keyBalanceAmountOperation,times(2)).get();
        verify(keyBalanceAmountOperation,times(2)).set(Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));

    }


    @Test
    void authorizationWithFallbackSuccessMovingCashBalance() {

        transactionDTO.setTotalAmount(new BigDecimal(10000));
        balanceCash.setAmount(new BigDecimal(10000));
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.init());
        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);

        Gson gson = new Gson();
        Transaction transaction = Transaction.builder()
                .payload(gson.toJson(transactionDTO))
                .insertedAt(LocalDateTime.now())
                .amount(transactionDTO.getTotalAmount())
                .accountId(account.getId())
                .balanceId(balanceFood.getId())
                .build();

        when(transactionRepository.save(transaction.toEntity())).thenReturn(transaction.toEntity());

        AuthorizationStatusEnum sut = transactionService.authorization(transactionDTO);
        assertThat(sut).isEqualTo(AuthorizationStatusEnum.APPROVED);
        assertThat(balanceCash.getAmount()).isEqualTo(new BigDecimal(0).setScale(2, RoundingMode.FLOOR));
        verify(keyBalanceAmountOperation,times(1)).get();
        verify(keyBalanceAmountOperation,times(2)).set(Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));

    }

    @Test
    void authorizationWithSuccessUsingBalanceAlreadyInCache() {

        transactionDTO.setTotalAmount(new BigDecimal(100));
        balanceFood.setAmount(new BigDecimal(900));
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.doneBalance(balanceFood.getAmount()));
        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);

        Gson gson = new Gson();
        Transaction transaction = Transaction.builder()
                .payload(gson.toJson(transactionDTO))
                .insertedAt(LocalDateTime.now())
                .amount(transactionDTO.getTotalAmount())
                .accountId(account.getId())
                .balanceId(balanceFood.getId())
                .build();

        when(transactionRepository.save(transaction.toEntity())).thenReturn(transaction.toEntity());

        AuthorizationStatusEnum sut = transactionService.authorization(transactionDTO);
        assertThat(sut).isEqualTo(AuthorizationStatusEnum.APPROVED);
        assertThat(balanceFood.getAmount()).isEqualTo(new BigDecimal(800).setScale(2, RoundingMode.FLOOR));
        verify(keyBalanceAmountOperation,times(1)).get();
        verify(keyBalanceAmountOperation,times(1)).set(Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));

    }

    @Test
    public void authorizationThrowsExceptionCauseAccountNotFound() {
        transactionDTO.setTotalAmount(new BigDecimal(100));
        when(accountService.findById(transactionDTO.getAccount())).thenThrow(EntityNotFoundException.class);

        assertThatThrownBy(() -> transactionService.authorization(transactionDTO)).isInstanceOf(EntityNotFoundException.class);

    }


    @Test
    void authorizationThrowsExceptionCauseAccountWithoutCashBalance() {

        transactionDTO.setTotalAmount(new BigDecimal(10000));
        account.getBalances().remove(1);

        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.init());

        assertThatThrownBy(() -> transactionService.authorization(transactionDTO)).isInstanceOf(EntityNotFoundException.class);
        verify(keyBalanceAmountOperation,times(1)).get();
        verify(keyBalanceAmountOperation,times(1)).set(Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));

    }


}