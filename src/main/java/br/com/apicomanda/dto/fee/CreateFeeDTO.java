package br.com.apicomanda.dto.fee;

import java.math.BigDecimal;

public record CreateFeeDTO(String name, BigDecimal percentage, Long userId) {
}
