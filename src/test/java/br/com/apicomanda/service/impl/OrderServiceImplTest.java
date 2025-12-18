package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Fee;
import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.domain.Order;
import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.OrderItemDTO;
import br.com.apicomanda.enums.StatusOrder;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private FeeRepository feeRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Deve salvar o pedido com sucesso calculando itens e taxas corretamente")
    void shouldSaveOrderSuccessfullyWithFees() {
        Long menuId = 1L;
        Long feeId = 10L;
        int quantity = 2;
        BigDecimal menuPrice = BigDecimal.valueOf(50.00);
        BigDecimal feePercentage = BigDecimal.valueOf(10.00);

        var itemDto = new OrderItemDTO(menuId, quantity);
        List<OrderItemDTO> items = List.of(itemDto);
        List<Long> feesIds = List.of(feeId);

        var requestDTO = new CreateOrderDTO(1, items, feesIds, "Sem cebola");

        var menu = Menu.builder().id(menuId).name("Pizza").price(menuPrice).build();
        var fee = Fee.builder().id(feeId).name("Serviço").percentage(feePercentage).build();

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(feeRepository.findAllById(feesIds)).thenReturn(List.of(fee));

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        BigDecimal expectedTotalOrderPrice = BigDecimal.valueOf(100.00); // 50 * 2
        BigDecimal expectedTotalFeesValue = BigDecimal.valueOf(10.00);   // 10% de 100
        BigDecimal expectedFinalPrice = BigDecimal.valueOf(110.00);      // 100 + 10

        assertNotNull(savedOrder);
        assertEquals(Integer.valueOf(1), savedOrder.getTableNumber());
        assertEquals("Sem cebola", savedOrder.getAdditionalComment());
        assertEquals(0, expectedTotalOrderPrice.compareTo(savedOrder.getTotalOrderPrice()));
        assertEquals(0, expectedTotalFeesValue.compareTo(savedOrder.getTotalFeesValue()));
        assertEquals(0, expectedFinalPrice.compareTo(savedOrder.getFinalTotalPrice()));
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(1, savedOrder.getAppliedFees().size());
    }

    @Test
    @DisplayName("Deve salvar o pedido com sucesso sem taxas aplicadas")
    void shouldSaveOrderSuccessfullyWithoutFees() {
        Long menuId = 1L;
        int quantity = 1;
        BigDecimal menuPrice = BigDecimal.valueOf(30.00);

        var itemDto = new OrderItemDTO(menuId, quantity);
        List<OrderItemDTO> items = List.of(itemDto);
        List<Long> feesIds = Collections.emptyList();

        var requestDTO = new CreateOrderDTO(2, items, feesIds, null);

        var menu = Menu.builder().id(menuId).name("Hamburguer").price(menuPrice).build();

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(savedOrder.getTotalOrderPrice()));
        assertEquals(BigDecimal.ZERO, savedOrder.getTotalFeesValue());
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(savedOrder.getFinalTotalPrice()));

        verify(feeRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o Menu não for encontrado")
    void shouldThrowExceptionWhenMenuNotFound() {
        Long invalidMenuId = 99L;
        var itemDto = new OrderItemDTO(invalidMenuId, 1);
        List<OrderItemDTO> items = List.of(itemDto);
        List<Long> feesIds = Collections.emptyList();

        var requestDTO = new CreateOrderDTO(1, items, feesIds, null);

        when(menuRepository.findById(invalidMenuId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.saveOrder(requestDTO);
        });

        assertEquals("Menu não encontrado: " + invalidMenuId, exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular corretamente quando lista de itens for vazia")
    void shouldHandleEmptyItemsList() {
        List<OrderItemDTO> items = Collections.emptyList();
        List<Long> feesIds = Collections.emptyList();

        var requestDTO = new CreateOrderDTO(1, items, feesIds, null);

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(BigDecimal.ZERO, savedOrder.getTotalOrderPrice());
        assertEquals(BigDecimal.ZERO, savedOrder.getFinalTotalPrice());
        assertTrue(savedOrder.getItems().isEmpty());
    }
}