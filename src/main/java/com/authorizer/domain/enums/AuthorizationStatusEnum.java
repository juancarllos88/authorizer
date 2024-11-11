package com.authorizer.domain.enums;

import lombok.Getter;

@Getter
public enum AuthorizationStatusEnum {

    APPROVED("00"),
    REJECTED("51"),
    ERROR("07");

    private final String code;

    AuthorizationStatusEnum(String code) {

        this.code = code;
    }

}
