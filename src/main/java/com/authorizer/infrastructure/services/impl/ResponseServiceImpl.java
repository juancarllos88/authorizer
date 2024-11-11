package com.authorizer.infrastructure.services.impl;

import com.authorizer.infrastructure.services.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseServiceImpl implements ResponseService {

    public <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }
}

