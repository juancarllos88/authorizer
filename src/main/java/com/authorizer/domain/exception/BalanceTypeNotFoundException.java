package com.authorizer.domain.exception;

public class BalanceTypeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BalanceTypeNotFoundException(String message) {
        super(message);
    }
}
