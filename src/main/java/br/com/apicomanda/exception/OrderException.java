package br.com.apicomanda.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}
