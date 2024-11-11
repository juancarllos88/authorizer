package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.Account;
import com.authorizer.infrastructure.entity.AccountEntity;
import com.authorizer.infrastructure.repository.AccountRepository;
import com.authorizer.domain.services.AccountService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountServiceImpl extends BaseServiceImpl<Account, AccountEntity> implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected AccountRepository getRepository() {
        return accountRepository;
    }

    @Override
    public Account findById(UUID id) {
        AccountEntity notFound = getRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("entity not found"));
      return notFound.toModel();
    }
}
