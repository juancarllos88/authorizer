package com.authorizer.application.controller;

import com.authorizer.application.contract.TransactionContract;
import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.domain.services.TransactionService;
import com.authorizer.infrastructure.services.ResponseService;
import com.authorizer.presentation.dto.response.ResponseDTO;
import com.authorizer.presentation.dto.transaction.TransactionDTO;
import com.authorizer.domain.services.impl.TransactionServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class TransactionController implements TransactionContract {

    private final TransactionService transactionService;
    private final ResponseService responseService;

    public TransactionController(TransactionService transactionService, ResponseService responseService) {
        this.transactionService = transactionService;
        this.responseService = responseService;
    }

    @Override
    public ResponseEntity<ResponseDTO> authorization(@Valid @RequestBody TransactionDTO transaction) throws Throwable {
        transactionService.authorization(transaction);
        return responseService.ok(new ResponseDTO(AuthorizationStatusEnum.APPROVED.getCode()));
    }

}
