package com.authorizer.application.it;

import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.infrastructure.filter.ConcurrentCacheControl;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.presentation.dto.transaction.TransactionResponseDTO;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = {"/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransactionIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    private static GenericContainer<?> redis;
    private static Jedis jedis;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine")).withExposedPorts(6379);
        redis.start();
        jedis = new Jedis(redis.getHost(), redis.getMappedPort(6379));
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        redis.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Test
    public void authorizationReturnsApprovedTransaction() throws IOException, InterruptedException {
        jedis.flushAll();
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .merchant("PADARIA DO ZE               SAO PAULO BR")
                .account(UUID.fromString("554e590b-a4bc-4859-b245-cbb4701fdbbd"))
                .totalAmount(new BigDecimal(1))
                .mcc("5811")
                .build();


        ResponseEntity<TransactionResponseDTO> sut = restTemplate.postForEntity("/transactions/authorization", transactionDTO, TransactionResponseDTO.class);

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody().code()).isEqualTo(AuthorizationStatusEnum.APPROVED.getCode());
        ConcurrentCacheControl concurrentCacheControl = new Gson().fromJson(jedis.get("KEY_BALANCE_FOOD_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControl.getBalanceAmount()).isEqualTo(new BigDecimal(9999).setScale(2, RoundingMode.FLOOR));
    }

    @Test
    public void authorizationReturnsRejectTransactionCauseInsufficientBalance() throws IOException, InterruptedException {
        jedis.flushAll();
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .merchant("PADARIA DO ZE               SAO PAULO BR")
                .account(UUID.fromString("554e590b-a4bc-4859-b245-cbb4701fdbbd"))
                .totalAmount(new BigDecimal(10001))
                .mcc("5811")
                .build();


        ResponseEntity<TransactionResponseDTO> sut = restTemplate.postForEntity("/transactions/authorization", transactionDTO, TransactionResponseDTO.class);

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody().code()).isEqualTo(AuthorizationStatusEnum.REJECTED.getCode());
        ConcurrentCacheControl concurrentCacheControlFood = new Gson().fromJson(jedis.get("KEY_BALANCE_FOOD_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControlFood.getBalanceAmount()).isEqualTo(new BigDecimal(10000).setScale(2, RoundingMode.FLOOR));
        ConcurrentCacheControl concurrentCacheControlCash = new Gson().fromJson(jedis.get("KEY_BALANCE_CASH_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControlCash.getBalanceAmount()).isEqualTo(new BigDecimal(10000).setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = {"/remove_balance.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void authorizationReturnsErrorTransactionCauseCashBalanceNotExist() throws IOException, InterruptedException {
        jedis.flushAll();
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .merchant("PADARIA DO ZE               SAO PAULO BR")
                .account(UUID.fromString("554e590b-a4bc-4859-b245-cbb4701fdbbd"))
                .totalAmount(new BigDecimal(10001))
                .mcc("5811")
                .build();


        ResponseEntity<TransactionResponseDTO> sut = restTemplate.postForEntity("/transactions/authorization", transactionDTO, TransactionResponseDTO.class);

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody().code()).isEqualTo(AuthorizationStatusEnum.ERROR.getCode());
        ConcurrentCacheControl concurrentCacheControlFood = new Gson().fromJson(jedis.get("KEY_BALANCE_FOOD_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControlFood.getBalanceAmount()).isEqualTo(new BigDecimal(10000).setScale(2, RoundingMode.FLOOR));
    }


    @Test
    @Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = {"/change_amount_balance.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void authorizationReturnsApprovedTransactionUsingCashBalanceFallback() throws IOException, InterruptedException {
        jedis.flushAll();
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .merchant("PADARIA DO ZE               SAO PAULO BR")
                .account(UUID.fromString("554e590b-a4bc-4859-b245-cbb4701fdbbd"))
                .totalAmount(new BigDecimal(9999))
                .mcc("5811")
                .build();


        ResponseEntity<TransactionResponseDTO> sut = restTemplate.postForEntity("/transactions/authorization", transactionDTO, TransactionResponseDTO.class);

        assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(sut.getBody().code()).isEqualTo(AuthorizationStatusEnum.APPROVED.getCode());
        ConcurrentCacheControl concurrentCacheControlFood = new Gson().fromJson(jedis.get("KEY_BALANCE_FOOD_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControlFood.getBalanceAmount()).isEqualTo(new BigDecimal(1000).setScale(2, RoundingMode.FLOOR));
        ConcurrentCacheControl concurrentCacheControlCash = new Gson().fromJson(jedis.get("KEY_BALANCE_CASH_ACCOUNT_554e590b-a4bc-4859-b245-cbb4701fdbbd"), ConcurrentCacheControl.class);
        assertThat(concurrentCacheControlCash.getBalanceAmount()).isEqualTo(new BigDecimal(1).setScale(2, RoundingMode.FLOOR));
    }

    @Test
    void CheckingRunningStatusRedisContainer() {
        assertThat(redis.isRunning()).isTrue();
    }



}