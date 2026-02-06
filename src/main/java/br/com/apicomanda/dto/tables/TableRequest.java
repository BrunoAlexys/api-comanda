package br.com.apicomanda.dto.tables;

public record TableRequest(
        Long numberTable,
        Long chairsAvailable,
        Long adminId
) {
}
