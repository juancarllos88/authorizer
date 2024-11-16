package com.authorizer.domain.services;

import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.model.Account;
import com.authorizer.domain.model.Transaction;
import com.authorizer.presentation.dto.transaction.TransactionDTO;

import java.util.UUID;

public interface TransactionService extends BaseService<Transaction> {

    AuthorizationStatusEnum authorization(TransactionDTO transaction);
}
