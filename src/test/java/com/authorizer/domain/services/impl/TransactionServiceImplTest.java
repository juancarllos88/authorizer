package com.authorizer.domain.services.impl;

import com.authorizer.domain.chain.steps.BalanceTypeStep;
import com.authorizer.domain.chain.steps.MerchantNameStep;
import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.enums.BalanceTypeEnum;
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
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        List<Balance> balances = List.of(balanceFood, balanceCash);
        account.setBalances(balances);


        Optional<Merchant> merchant = Optional.of(new Merchant(UUID.randomUUID(), "PADARIA", BalanceTypeEnum.FOOD));

        when(accountService.findById(transactionDTO.getAccount())).thenReturn(account);

        when(merchantService.findByName(transactionDTO.getMerchant())).thenReturn(merchant);

    }

    @Test
    void authorizationWithSuccess() {

        transactionDTO.setTotalAmount(BigDecimal.TEN);
        BoundValueOperations<String, ConcurrentCacheControl> boundValueOperations = Mockito.mock(BoundValueOperations.class);

        when(redisTemplate.boundValueOps(Mockito.anyString())).thenReturn(boundValueOperations);

        ValueOperations<String, ConcurrentCacheControl> valueOperations = Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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

    }


    @Test
    void authorizationThrowsInsufficientBalanceException() {

        transactionDTO.setTotalAmount(new BigDecimal(10000));
        BoundValueOperations<String, ConcurrentCacheControl> keyBalanceAmountOperation = Mockito.mock(BoundValueOperations.class);
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.init());
        when(redisTemplate.boundValueOps(Mockito.anyString())).thenReturn(keyBalanceAmountOperation);

        ValueOperations<String, ConcurrentCacheControl> valueOperations = Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.any(ConcurrentCacheControl.class), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);


        assertThatThrownBy(() -> transactionService.authorization(transactionDTO)).isInstanceOf(InsufficientBalanceException.class);

    }


    @Test
    void authorizationWithSuccessMovingCashBalance() {

        transactionDTO.setTotalAmount(new BigDecimal(10000));
        balanceCash.setAmount(new BigDecimal(10000));

        BoundValueOperations<String, ConcurrentCacheControl> keyBalanceAmountOperation = Mockito.mock(BoundValueOperations.class);
        when(keyBalanceAmountOperation.get()).thenReturn(ConcurrentCacheControl.init());
        when(redisTemplate.boundValueOps(Mockito.anyString())).thenReturn(keyBalanceAmountOperation);

        ValueOperations<String, ConcurrentCacheControl> valueOperations = Mockito.mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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

    }


}