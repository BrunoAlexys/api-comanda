package br.com.apicomanda.service;

public interface RedisSequenceService {
    String getNextOrderNumber(Long adminId);
}
