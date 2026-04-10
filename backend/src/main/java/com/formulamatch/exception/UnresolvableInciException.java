package com.formulamatch.exception;

import java.util.List;

public class UnresolvableInciException extends RuntimeException {

    public UnresolvableInciException(List<String> unknown) {
        super("Unknown INCI names: " + String.join(", ", unknown));
    }
}
