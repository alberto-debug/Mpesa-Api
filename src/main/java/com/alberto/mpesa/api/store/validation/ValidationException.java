package com.alberto.mpesa.api.store.validation;

public class ValidationException extends RuntimeException {

    public ValidationException(String message){
        super(message);
    }
}
