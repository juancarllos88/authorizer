package com.authorizer.infrastructure.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.join;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentCacheControl implements Serializable {
    public Map<String, Object> header;
    public int status;
    public String cacheValue;
    public boolean isDone;


    protected static ConcurrentCacheControl init() {
        return new ConcurrentCacheControl(Collections.emptyMap(), 0, "", false);
    }

    protected static ConcurrentCacheControl done(Map<String, Object> header, Integer status, String cacheValue) {
        return new ConcurrentCacheControl(header, status, cacheValue, true);
    }

}
