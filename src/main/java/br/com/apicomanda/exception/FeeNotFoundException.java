package br.com.apicomanda.exception;

public class FeeNotFoundException extends RuntimeException {
    public FeeNotFoundException(String message) {
        super(message);
    }
}
