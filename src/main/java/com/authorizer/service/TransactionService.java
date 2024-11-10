package com.authorizer.service;

import com.authorizer.chain.BalanceTypeStep;
import com.authorizer.domain.Account;
import com.authorizer.dto.TransactionDTO;
import com.authorizer.service.impl.AccountServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
public class TransactionService {


    private final AccountServiceImpl accountServiceImpl;
    private final List<BalanceTypeStep> stepsToFindBalanceType;

    public TransactionService(List<BalanceTypeStep> stepsToFindBalanceType, AccountServiceImpl accountServiceImpl) {
        this.stepsToFindBalanceType = stepsToFindBalanceType;
        this.accountServiceImpl = accountServiceImpl;
    }

    public void processing(TransactionDTO transaction) throws Throwable {
        log.info("Starting transaction...");
        Account account = accountServiceImpl.findById(transaction.getAccount());

        Optional<BalanceTypeStep> first = stepsToFindBalanceType
                .stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .filter(next -> next.searchInfo(transaction)).findFirst();
        System.out.println(first.get().getBalanceTypeEnum());
    }



    }




