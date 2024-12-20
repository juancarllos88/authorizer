package com.authorizer.domain.model;

import java.io.Serializable;


public abstract class BaseModel<T extends Serializable, E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected T id;

    public BaseModel() {
        super();
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseModel<?, ?> other = (BaseModel<?, ?>) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }

    public abstract E toEntity();
}
