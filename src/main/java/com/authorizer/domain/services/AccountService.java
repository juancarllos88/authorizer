package com.authorizer.domain.services;

import com.authorizer.domain.model.Account;

import java.util.UUID;

public interface AccountService  extends BaseService<Account> {

    Account findById(UUID id);
}
