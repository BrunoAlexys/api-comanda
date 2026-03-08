package br.com.apicomanda.exception;

public class ObjectAlreadyRegisteredException extends RuntimeException {
    public ObjectAlreadyRegisteredException(String message) {
        super(message);
    }
}
