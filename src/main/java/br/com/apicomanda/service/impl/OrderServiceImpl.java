package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.*;
import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.KitchenOrderDTO;
import br.com.apicomanda.dto.order.OrderItemDTO;
import br.com.apicomanda.enums.StatusOrder;
import br.com.apicomanda.exception.MenuException;
import br.com.apicomanda.exception.OrderException;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.repository.OrderRepository;
import br.com.apicomanda.service.AdminService;
import br.com.apicomanda.service.EmployeeService;
import br.com.apicomanda.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final FeeRepository feeRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AdminService adminService;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    @CacheEvict(value = "kitchenOrders", allEntries = true)
    public void saveOrder(CreateOrderDTO request) {
        List<OrderItem> orderItems = createOrderItems(request.items());
        BigDecimal totalOrderPrice = calculateTotalOrderPrice(orderItems);
        List<OrderFee> orderFees = createOrderFees(request.appliedFeeIds(), totalOrderPrice);
        BigDecimal totalFeesValue = calculateTotalFeesValue(orderFees);
        BigDecimal finalTotalPrice = totalOrderPrice.add(totalFeesValue);

        var employee = employeeService.getEmployeeById(request.userId());
        var admin = this.adminService.getAdminById(employee.getAdmin().getId());

        Order order = buildOrder(request, orderItems, orderFees, totalOrderPrice, totalFeesValue, finalTotalPrice, StatusOrder.PENDING, admin, employee);
        Order savedOrder = this.orderRepository.save(order);

        notifyKitchen(savedOrder);
    }

    @Override
    @Cacheable(value = "kitchenOrders", key = "#a0")
    public List<KitchenOrderDTO> fetchOrderByDate(Long adminId) {
        log.info("Fetching orders for user {}", adminId);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return this.orderRepository.findByAdminIdAndCreatedAtBetween(adminId, startOfDay, endOfDay)
                .stream()
                .map(this::mapToKitchenDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "kitchenOrders", allEntries = true)
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Pedido não encontrado: " + orderId));

        try {
            StatusOrder newStatusEnum = StatusOrder.valueOf(newStatus.toUpperCase());
            if (newStatusEnum == StatusOrder.DOING && order.getStartedAt() == null) {
                order.setStartedAt(LocalDateTime.now());
            }

            if (newStatusEnum == StatusOrder.DONE) {
                order.setFinishedAt(LocalDateTime.now());
            }

            order.setStatusOrder(newStatusEnum);
            Order orderUpdate = this.orderRepository.save(order);
            notifyKitchen(orderUpdate);
        } catch (IllegalArgumentException e) {
            throw new OrderException("Status inválido: " + newStatus);
        }
    }

    @Override
    public Double calculateAverageTime(Long adminId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        Double averageSeconds = orderRepository.getAveragePreparationTimeInSeconds(adminId, startOfDay, endOfDay);

        if (averageSeconds == null) {
            return 0.0;
        }

        return averageSeconds / 60.0;
    }

    private KitchenOrderDTO mapToKitchenDTO(Order order) {
        List<String> itemDescriptions = order.getItems().stream()
                .map(item -> item.getQuantity() + "x " + item.getMenu().getName())
                .toList();

        String formattedTotal = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                .format(order.getFinalTotalPrice());

        String formattedTime = order.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm"));

        String finisheAtIso = order.getFinishedAt() != null
                ? order.getFinishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;

        return new KitchenOrderDTO(
                order.getId().toString(),
                "#" + String.format("%04d", order.getId()),
                "Mesa " + order.getTableNumber(),
                itemDescriptions,
                formattedTotal,
                formattedTime,
                order.getStatusOrder().name(),
                finisheAtIso
        );
    }

    private List<OrderFee> createOrderFees(List<Long> feeIds, BigDecimal basePrice) {
        if (feeIds == null || feeIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Fee> feeDefinitions = feeRepository.findAllById(feeIds);
        List<OrderFee> calculatedFees = new ArrayList<>();

        for (Fee def : feeDefinitions) {
            BigDecimal calculatedValue = BigDecimal.ZERO;

            if (def.getPercentage() != null) {
                calculatedValue = basePrice
                        .multiply(def.getPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
            }

            OrderFee orderFee = OrderFee.builder()
                    .name(def.getName())
                    .amount(calculatedValue)
                    .build();

            calculatedFees.add(orderFee);
        }

        return calculatedFees;
    }

    private BigDecimal calculateTotalFeesValue(List<OrderFee> orderFees) {
        return orderFees.stream()
                .map(OrderFee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> createOrderItems(List<OrderItemDTO> itemsDto) {
        if (itemsDto == null || itemsDto.isEmpty()) return Collections.emptyList();
        return itemsDto.stream().map(this::mapToOrderItem).collect(Collectors.toList());
    }

    private OrderItem mapToOrderItem(OrderItemDTO itemDTO) {
        Menu menu = this.menuRepository.findById(itemDTO.menuId())
                .orElseThrow(() -> new MenuException("Menu não encontrado: " + itemDTO.menuId()));
        return OrderItem.builder()
                .menu(menu)
                .quantity(itemDTO.quantity())
                .price(menu.getPrice())
                .build();
    }

    private BigDecimal calculateTotalOrderPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(CreateOrderDTO request,
                             List<OrderItem> items,
                             List<OrderFee> fees,
                             BigDecimal totalOrderPrice,
                             BigDecimal totalFeesValue,
                             BigDecimal finalTotalPrice,
                             StatusOrder statusOrder,
                             Admin admin,
                             Employee employee
    ) {
        return Order.builder()
                .tableNumber(request.tableNumber())
                .items(items)
                .appliedFees(fees)
                .additionalComment(request.additionalComment())
                .totalOrderPrice(totalOrderPrice)
                .totalFeesValue(totalFeesValue)
                .finalTotalPrice(finalTotalPrice)
                .statusOrder(statusOrder)
                .admin(admin)
                .employee(employee)
                .build();
    }

    private void notifyKitchen(Order order) {
        KitchenOrderDTO wsDto = mapToKitchenDTO(order);
        simpMessagingTemplate.convertAndSend("/topic/orders", wsDto);
    }
}