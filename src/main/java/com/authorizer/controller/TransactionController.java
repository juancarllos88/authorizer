package com.authorizer.controller;

import com.authorizer.contract.TransactionContract;
import com.authorizer.dto.TransactionDTO;
import com.authorizer.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class TransactionController implements TransactionContract {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<String> authorization(TransactionDTO transaction) throws Throwable {
        transactionService.processing(transaction);
        return null;
    }


}
