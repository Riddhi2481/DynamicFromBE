package com.example.praticalTestBE.exception;

public class InvalidVersionException extends RuntimeException {
    public InvalidVersionException(String message) {
        super(message);
    }
}
