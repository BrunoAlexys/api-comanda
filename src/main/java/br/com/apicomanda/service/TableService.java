package br.com.apicomanda.service;

import br.com.apicomanda.dto.tables.TableRequest;
import br.com.apicomanda.dto.tables.TablesResponse;

import java.util.List;

public interface TableService {
    void createTable(TableRequest tableRequest);
    List<TablesResponse> findAllTables(Long adminId);
}
