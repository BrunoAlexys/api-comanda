package br.com.apicomanda.exception;

public class CategoryNotFound extends RuntimeException {
    public  CategoryNotFound(String message) {
        super(message);
    }
}
