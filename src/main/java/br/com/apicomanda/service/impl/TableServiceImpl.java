package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Tables;
import br.com.apicomanda.dto.tables.TableRequest;
import br.com.apicomanda.dto.tables.TablesResponse;
import br.com.apicomanda.enums.StatusTable;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.repository.TablesRepository;
import br.com.apicomanda.service.TableService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    private final TablesRepository tablesRepository;
    private final AdminRepository adminRepository;

    @Override
    @Transactional
    @CacheEvict(value = "tables_fix", allEntries = true)
    public void createTable(TableRequest tableRequest) {
        var admin = this.adminRepository.findById(tableRequest.adminId())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        var table = Tables.builder()
                .numberTable(tableRequest.numberTable())
                .chairsAvailable(tableRequest.chairsAvailable())
                .status(StatusTable.AVAILABLE)
                .admin(admin)
                .build();

        this.tablesRepository.save(table);
    }

    @Override
    @Cacheable(value = "tables_fix", key = "#adminId")
    public List<TablesResponse> findAllTables(Long adminId) {
        List<Tables> list = this.tablesRepository.findAllByAdminId(adminId);
        return new ArrayList<>(list.stream()
                .map(TablesResponse::new)
                .toList());
    }
}