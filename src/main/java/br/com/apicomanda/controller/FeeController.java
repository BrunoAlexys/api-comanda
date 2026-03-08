package br.com.apicomanda.controller;

import br.com.apicomanda.dto.fee.CreateFeeDTO;
import br.com.apicomanda.dto.fee.FeeResponseDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @PostMapping
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<Void> createFee(@RequestBody @Valid CreateFeeDTO request) {
        this.feeService.createFee(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/user/{adminId}")
    @PreAuthorize(ApplicationConstants.IS_ADMIN_OR_USER)
    public ResponseEntity<List<FeeResponseDTO>> findAllFeeAdmin(@PathVariable("adminId") Long adminId) {
        List<FeeResponseDTO> feeList = this.feeService.findAllById(adminId);
        return new ResponseEntity<>(feeList, HttpStatus.OK);
    }
}
