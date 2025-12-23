package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Fee;
import br.com.apicomanda.dto.fee.CreateFeeDTO;
import br.com.apicomanda.dto.fee.FeeResponseDTO;
import br.com.apicomanda.exception.FeeNotFoundException;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.service.FeeService;
import br.com.apicomanda.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeRepository feeRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void createFee(CreateFeeDTO requestDto) {
        var user = this.userService.getUserById(requestDto.userId());
        var fee = Fee.builder()
                .name(requestDto.name())
                .percentage(requestDto.percentage())
                .user(user)
                .build();
        this.feeRepository.save(fee);
    }

    @Override
    public List<FeeResponseDTO> findAllById(Long id) {
        List<Fee> fees = feeRepository.findByUserId(id);

        return fees.stream()
                .map(fee -> new FeeResponseDTO(
                        fee.getId(),
                        fee.getName(),
                        fee.getPercentage()
                ))
                .toList();
    }

    @Override
    public FeeResponseDTO findById(Long id) {
        Optional<Fee> fee = feeRepository.findById(id);
        return fee.map(value -> new FeeResponseDTO(value.getId(), value.getName(), value.getPercentage()))
                .orElseThrow(() -> new FeeNotFoundException("Fee não encontrada!"));
    }
}
