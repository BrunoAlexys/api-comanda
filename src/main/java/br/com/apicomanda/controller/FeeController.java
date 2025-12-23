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

    @GetMapping("/user/{userId}")
    @PreAuthorize(ApplicationConstants.IS_ADMIN)
    public ResponseEntity<List<FeeResponseDTO>> findAllFeeUser(@PathVariable("userId") Long userId) {
        List<FeeResponseDTO> feeList = this.feeService.findAllById(userId);
        return new ResponseEntity<>(feeList, HttpStatus.OK);
    }
}
