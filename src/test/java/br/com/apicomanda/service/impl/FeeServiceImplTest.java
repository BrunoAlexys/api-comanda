package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Fee;
import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.dto.fee.CreateFeeDTO;
import br.com.apicomanda.dto.fee.FeeResponseDTO;
import br.com.apicomanda.exception.FeeNotFoundException;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeServiceImplTest {

    @Mock
    private FeeRepository feeRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private FeeServiceImpl feeService;

    @Test
    @DisplayName("Deve criar uma Taxa com sucesso associada a um usuário")
    void shouldCreateFeeSuccessfully() {
        Long userId = 1L;
        var requestDTO = new CreateFeeDTO("Taxa de Serviço", BigDecimal.valueOf(10.0), userId);
        var user = new Admin();
        user.setId(userId);

        when(adminService.getAdminById(userId)).thenReturn(user);

        feeService.createFee(requestDTO);

        verify(adminService, times(1)).getAdminById(userId);
        verify(feeRepository, times(1)).save(any(Fee.class));
    }

    @Test
    @DisplayName("Deve listar todas as Taxas de um usuário específico")
    void shouldFindAllFeesByUserId() {
        Long userId = 1L;
        var fee1 = Fee.builder().id(10L).name("Taxa 1").percentage(BigDecimal.valueOf(5)).build();
        var fee2 = Fee.builder().id(20L).name("Taxa 2").percentage(BigDecimal.valueOf(10)).build();
        var fees = List.of(fee1, fee2);

        when(feeRepository.findByAdminId(userId)).thenReturn(fees);

        List<FeeResponseDTO> responseList = feeService.findAllById(userId);

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals(fee1.getName(), responseList.get(0).name());
        assertEquals(fee2.getName(), responseList.get(1).name());

        verify(feeRepository, times(1)).findByAdminId(userId);
    }

    @Test
    @DisplayName("Deve retornar uma Taxa DTO com sucesso quando o ID existir")
    void shouldReturnFeeDtoWhenIdExists() {
        long feeId = 1L;
        var fee = Fee.builder()
                .id(feeId)
                .name("Couvert")
                .percentage(BigDecimal.valueOf(15.0))
                .build();

        when(feeRepository.findById(feeId)).thenReturn(Optional.of(fee));

        FeeResponseDTO responseDTO = feeService.findById(feeId);

        assertNotNull(responseDTO);
        assertEquals(fee.getId(), responseDTO.id());
        assertEquals(fee.getName(), responseDTO.name());
        assertEquals(fee.getPercentage(), responseDTO.percentage());

        verify(feeRepository, times(1)).findById(feeId);
    }

    @Test
    @DisplayName("Deve lançar FeeNotFoundException quando a Taxa não for encontrada")
    void shouldThrowFeeNotFoundExceptionWhenFeeDoesNotExist() {
        long nonExistentId = 99L;

        when(feeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        var exception = assertThrows(FeeNotFoundException.class, () -> {
            feeService.findById(nonExistentId);
        });

        assertEquals("Taxa não encontrada!", exception.getMessage());

        verify(feeRepository, times(1)).findById(nonExistentId);
    }
}