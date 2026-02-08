package br.com.apicomanda.controller;

import br.com.apicomanda.dto.tables.TableRequest;
import br.com.apicomanda.dto.tables.TablesResponse;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<Void> createTable(@RequestBody @Valid TableRequest request) {
        this.tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("{adminId}")
    @PreAuthorize(ApplicationConstants.IS_ADMIN_OR_USER)
    public ResponseEntity<List<TablesResponse>> getAllTables(@PathVariable("adminId") Long adminId) {
        var tables = this.tableService.findAllTables(adminId);
        return ResponseEntity.ok(tables);
    }
}
