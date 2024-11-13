package com.authorizer.infrastructure.filter;

import com.authorizer.domain.enums.AuthorizationStatusEnum;
import com.authorizer.presentation.dto.response.ResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.join;

import org.apache.commons.lang3.StringUtils;

@Log4j2
@RequiredArgsConstructor
public class ConcurrentTransactionFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_TOKEN = "idempotency-token";

    public static final String DELIMITER = "_";

    private final RedisTemplate<String, ConcurrentCacheControl> redisTemplate;

    private final long timeToLive;

    private final ObjectMapper OBJECT_MAPPER = initObjectMapper();

    private ObjectMapper initObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String idempotencyToken = request.getHeader(IDEMPOTENCY_TOKEN);

        log.debug("Start IdempotenceFilter {}", idempotencyToken);
        String idempotenceCacheKey = join(DELIMITER, method, request.getRequestURI(), idempotencyToken);

        if (isNotTargetMethod(method)) {
            log.info("Request method {} didn't match the target idempotency https method.", method);
            filterChain.doFilter(request, response);

        } else if (StringUtils.isBlank(idempotencyToken)) {
            log.warn("Request should bring a idempotencyToken in header, but no. get idempotenceCacheKey as {}.", idempotenceCacheKey);
            filterChain.doFilter(request, response);

        } else {
            log.info("idempotency-token not empty {}", idempotencyToken);
            BoundValueOperations<String, ConcurrentCacheControl> keyIdempotenceOperation = redisTemplate.boundValueOps(idempotenceCacheKey);

            boolean isAbsent = keyIdempotenceOperation.setIfAbsent(ConcurrentCacheControl.init(), timeToLive, TimeUnit.MINUTES);
            if (isAbsent) {
                log.info("cache {} not exist ", idempotenceCacheKey);

                CachedRequestHttpServletRequest requestCopier = new CachedRequestHttpServletRequest(request);
                String accountFromRequest = getAccountFromRequest(requestCopier);

                String concurrencyAccountCacheKey = "CONCURRENCY_ACCOUNT_CACHE_KEY_" + accountFromRequest;
                BoundValueOperations<String, ConcurrentCacheControl> keyAccountOperation = redisTemplate.boundValueOps(concurrencyAccountCacheKey);
                //boolean isAccountIsProcessing = keyAccountOperation.setIfAbsent(CacheControl.init(), 100, TimeUnit.MILLISECONDS);
                boolean isAccountIsProcessing = keyAccountOperation.setIfAbsent(ConcurrentCacheControl.init(), 1, TimeUnit.MINUTES);
                if (!isAccountIsProcessing) {
                    //decline txn concurrency
                    handleWhenConcurrencyExist(response, concurrencyAccountCacheKey);
                    return;
                }

                ContentCachingResponseWrapper responseCopier = new ContentCachingResponseWrapper(response);

                filterChain.doFilter(requestCopier, responseCopier);

                updateResultInCache(idempotenceCacheKey, responseCopier, keyIdempotenceOperation);
                responseCopier.copyBodyToResponse();
            } else {
                log.info("cache {} already exist ", idempotenceCacheKey);
                handleWhenCacheExist(request, response, keyIdempotenceOperation, idempotenceCacheKey);
            }
        }
    }

    private boolean isNotTargetMethod(String method) {
        return !HttpMethod.POST.matches(method);
    }

    private void updateResultInCache(String cacheKey, ContentCachingResponseWrapper responseCopier,
                                     BoundValueOperations<String, ConcurrentCacheControl> keyOperation)
            throws UnsupportedEncodingException {

        if (needCache(responseCopier)) {
            log.info("response to cache {} need to be cached", cacheKey);
            String responseBody = new String(responseCopier.getContentAsByteArray(), responseCopier.getCharacterEncoding());
            ConcurrentCacheControl result = ConcurrentCacheControl.done(Collections.emptyMap(), responseCopier.getStatus(), responseBody);

            log.info("update cache {} ", cacheKey);
            keyOperation.set(result, timeToLive, TimeUnit.MINUTES);
        } else {
            log.info("process result don't need to be cached");
            redisTemplate.delete(keyOperation.getKey());
        }
    }

    private void handleWhenCacheExist(HttpServletRequest request, HttpServletResponse response,
                                      BoundValueOperations<String, ConcurrentCacheControl> keyOperation, String cacheKey)
            throws IOException {
        ConcurrentCacheControl cachedResponse = keyOperation.get();
        log.info("cached content = {} ", cachedResponse);
        String responseBody;
        Integer status;

        if (cachedResponse.isDone) {
            log.info("cache {} exist, and is done.", cacheKey);
            status = cachedResponse.status;
            responseBody = cachedResponse.cacheValue;
        } else {
            log.info("cache {} exist, and is still in processing, please retry later", cacheKey);
            /*status = HttpStatus.TOO_EARLY.value();
            ProblemDetail detail = new ProblemDetail();
            detail.setStatus(HttpStatus.TOO_EARLY);
            detail.setType(URI.create(request.getRequestURI()));
            detail.setDetail("request is now processing, please try again later");*/
            status = HttpStatus.OK.value();
            ResponseDTO responseDTO = new ResponseDTO(AuthorizationStatusEnum.ERROR.getCode());
            responseBody = OBJECT_MAPPER.writeValueAsString(responseDTO);
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        PrintWriter responseWriter = response.getWriter();
        responseWriter.write(responseBody);

        response.flushBuffer();

    }

    private void handleWhenConcurrencyExist(HttpServletResponse response, String cacheKey)
            throws IOException {

        log.info("cache {} exist, and is still in processing, please retry later", cacheKey);
        ResponseDTO responseDTO = new ResponseDTO(AuthorizationStatusEnum.ERROR.getCode());
        String responseBody = OBJECT_MAPPER.writeValueAsString(responseDTO);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        PrintWriter responseWriter = response.getWriter();
        responseWriter.write(responseBody);

        response.flushBuffer();
    }

    private boolean needCache(ContentCachingResponseWrapper responseCopier) {
        int statusCode = responseCopier.getStatus();
        return statusCode >= 200
                && statusCode < 300;
    }

    private String getAccountFromRequest(HttpServletRequest request) throws IOException {
        byte[] inputStreamBytes = StreamUtils.copyToByteArray(request.getInputStream());
        Map<String, String> jsonRequest = new ObjectMapper().readValue(inputStreamBytes, Map.class);
        return jsonRequest.get("account");
    }


}
