package br.com.apicomanda.service;

import br.com.apicomanda.dto.fee.CreateFeeDTO;
import br.com.apicomanda.dto.fee.FeeResponseDTO;

import java.util.List;

public interface FeeService {
    void createFee(CreateFeeDTO requestDto);
    List<FeeResponseDTO> findAllById(Long id);
    FeeResponseDTO findById(Long id);
}
