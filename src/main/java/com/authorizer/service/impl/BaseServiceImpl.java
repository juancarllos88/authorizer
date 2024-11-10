package com.authorizer.service.impl;

import com.authorizer.repository.BaseRepository;
import com.authorizer.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public abstract class BaseServiceImpl<T> implements BaseService<T> {

    protected abstract BaseRepository getRepository();

    @Override
    @Transactional(readOnly = true)
    public T findById(UUID id) throws Throwable {
        return (T) getRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("not found"));
    }

}
