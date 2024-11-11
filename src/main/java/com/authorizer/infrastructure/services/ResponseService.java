package com.authorizer.infrastructure.services;

import org.springframework.http.ResponseEntity;

public interface ResponseService {

    <T> ResponseEntity<T> ok(T data);

}

