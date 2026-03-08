package br.com.apicomanda.exception;

public class UserUnauthorizedExecption extends RuntimeException {
    public UserUnauthorizedExecption(String message) {
        super(message);
    }
}
