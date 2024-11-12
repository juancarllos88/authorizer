package com.authorizer.infrastructure.filter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.net.URI;

@Data
@Getter
@Setter
public class ProblemDetail {
    private String title;
    private HttpStatus status;
    private String detail;
    private URI type;
    private String instance;

}
