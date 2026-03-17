package br.com.apicomanda.service.impl;

import br.com.apicomanda.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RedisSequenceServiceImpl implements RedisSequenceService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public String getNextOrderNumber(Long adminId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "order_seq:" + adminId + ":" + date;

        Long sequence = redisTemplate.opsForValue().increment(key);

        redisTemplate.expire(key, Duration.ofDays(2));

        return String.format("%04d", sequence);
    }
}
