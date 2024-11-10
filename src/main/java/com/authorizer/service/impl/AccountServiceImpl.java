package com.authorizer.service.impl;

import com.authorizer.domain.Account;
import com.authorizer.repository.AccountRepository;
import com.authorizer.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends BaseServiceImpl<Account> implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected AccountRepository getRepository() {
        return accountRepository;
    }
}
