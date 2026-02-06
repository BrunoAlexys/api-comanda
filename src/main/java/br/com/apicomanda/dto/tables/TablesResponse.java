package br.com.apicomanda.dto.tables;

import br.com.apicomanda.domain.Tables;

public record TablesResponse(
        Long id,
        Long numberTable,
        Long chairsAvailable,
        String status
) {
    public TablesResponse(Tables tables) {
        this(
                tables.getId(),
                tables.getNumberTable(),
                tables.getChairsAvailable(),
                tables.getStatus().name()
        );
    }
}
