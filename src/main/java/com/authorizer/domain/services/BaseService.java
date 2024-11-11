package com.authorizer.domain.services;

public interface BaseService<T> {

    T save(T model);

}
