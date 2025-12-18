package br.com.apicomanda.dto.fee;

import java.math.BigDecimal;

public record FeeResponseDTO(Long id, String name, BigDecimal percentage) {
}
