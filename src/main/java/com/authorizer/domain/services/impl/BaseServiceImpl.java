package com.authorizer.domain.services.impl;

import com.authorizer.domain.model.BaseModel;
import com.authorizer.infrastructure.entity.BaseEntity;
import com.authorizer.infrastructure.repository.BaseRepository;
import com.authorizer.domain.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public abstract class BaseServiceImpl<T extends BaseModel, E extends BaseEntity> implements BaseService<T> {

    protected abstract BaseRepository getRepository();

    @Override
    @Transactional
    public T save(T model) {
        E entity = (E) getRepository().save(model.toEntity());
        return (T) entity.toModel();
    }


}
