package br.com.apicomanda.exception;

public class NotFounException extends RuntimeException {
    public NotFounException(String message) {
        super(message);
    }
}
