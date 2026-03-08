package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.*;
import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.OrderItemDTO;
import br.com.apicomanda.exception.MenuException;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.repository.OrderRepository;
import br.com.apicomanda.service.AdminService;
import br.com.apicomanda.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Mock
    private AdminService adminService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

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

        var requestDTO = new CreateOrderDTO(1, items, feesIds, "Sem cebola", 2L);

        var menu = Menu.builder().id(menuId).name("Pizza").price(menuPrice).build();
        var fee = Fee.builder().id(feeId).name("Serviço").percentage(feePercentage).build();

        var user = new Admin();
        user.setId(1L);

        var employee = new Employee();
        employee.setId(2L);
        employee.setAdmin(user);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(feeRepository.findAllById(feesIds)).thenReturn(List.of(fee));
        when(adminService.getAdminById(anyLong())).thenReturn(user);
        when(employeeService.getEmployeeById(anyLong())).thenReturn(employee);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        BigDecimal expectedTotalOrderPrice = BigDecimal.valueOf(100.00);
        BigDecimal expectedTotalFeesValue = BigDecimal.valueOf(10.00);
        BigDecimal expectedFinalPrice = BigDecimal.valueOf(110.00);

        assertNotNull(savedOrder);
        assertEquals(Integer.valueOf(1), savedOrder.getTableNumber());
        assertEquals("Sem cebola", savedOrder.getAdditionalComment());
        assertEquals(0, expectedTotalOrderPrice.compareTo(savedOrder.getTotalOrderPrice()));
        assertEquals(0, expectedTotalFeesValue.compareTo(savedOrder.getTotalFeesValue()));
        assertEquals(0, expectedFinalPrice.compareTo(savedOrder.getFinalTotalPrice()));
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(1, savedOrder.getAppliedFees().size());

        verify(simpMessagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
        verify(employeeService, times(1)).getEmployeeById(anyLong());
        verify(adminService, times(1)).getAdminById(anyLong());
        verify(orderRepository, times(1)).save(orderCaptor.capture());
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

        var requestDTO = new CreateOrderDTO(2, items, feesIds, null, 2L);

        var menu = Menu.builder().id(menuId).name("Hamburguer").price(menuPrice).build();

        var user = new Admin();
        user.setId(2L);

        var employee = new Employee();
        employee.setId(2L);
        employee.setAdmin(user);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(adminService.getAdminById(anyLong())).thenReturn(user);
        when(employeeService.getEmployeeById(anyLong())).thenReturn(employee);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(2L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(savedOrder.getTotalOrderPrice()));
        assertEquals(BigDecimal.ZERO, savedOrder.getTotalFeesValue());
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(savedOrder.getFinalTotalPrice()));

        verify(feeRepository, never()).findAllById(any());
        verify(employeeService, times(1)).getEmployeeById(anyLong());
        verify(adminService, times(1)).getAdminById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o Menu não for encontrado")
    void shouldThrowExceptionWhenMenuNotFound() {
        Long invalidMenuId = 99L;
        var itemDto = new OrderItemDTO(invalidMenuId, 1);
        List<OrderItemDTO> items = List.of(itemDto);
        List<Long> feesIds = Collections.emptyList();

        var requestDTO = new CreateOrderDTO(1, items, feesIds, null,  2L);

        when(menuRepository.findById(invalidMenuId)).thenReturn(Optional.empty());

        MenuException exception = assertThrows(MenuException.class, () -> {
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

        var requestDTO = new CreateOrderDTO(1, items, feesIds, null, 2L);

        var user = new Admin();
        user.setId(1L);

        var employee = new Employee();
        employee.setId(2L);
        employee.setAdmin(user);

        when(adminService.getAdminById(anyLong())).thenReturn(user);
        when(employeeService.getEmployeeById(anyLong())).thenReturn(employee);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(3L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        orderService.saveOrder(requestDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(BigDecimal.ZERO, savedOrder.getTotalOrderPrice());
        assertEquals(BigDecimal.ZERO, savedOrder.getFinalTotalPrice());
        assertTrue(savedOrder.getItems().isEmpty());

        verify(orderRepository, times(1)).save(orderCaptor.capture());
        verify(employeeService, times(1)).getEmployeeById(anyLong());
        verify(adminService, times(1)).getAdminById(anyLong());
    }
}