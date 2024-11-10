package com.authorizer.service;

import java.util.UUID;

public interface BaseService<T> {

    T findById(UUID id) throws Throwable;
    T save(T entity);

}
