package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.*;
import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.OrderItemDTO;
import br.com.apicomanda.enums.StatusOrder;
import br.com.apicomanda.repository.FeeRepository;
import br.com.apicomanda.repository.MenuRepository;
import br.com.apicomanda.repository.OrderRepository;
import br.com.apicomanda.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final FeeRepository feeRepository;

    @Override
    @Transactional
    public void saveOrder(CreateOrderDTO request) {
        List<OrderItem> orderItems = createOrderItems(request.items());
        BigDecimal totalOrderPrice = calculateTotalOrderPrice(orderItems);

        List<OrderFee> orderFees = createOrderFees(request.appliedFeeIds(), totalOrderPrice);

        BigDecimal totalFeesValue = calculateTotalFeesValue(orderFees);

        BigDecimal finalTotalPrice = totalOrderPrice.add(totalFeesValue);

        Order order = buildOrder(request, orderItems, orderFees, totalOrderPrice, totalFeesValue, finalTotalPrice, StatusOrder.PENDING);

        this.orderRepository.save(order);
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
                .orElseThrow(() -> new IllegalArgumentException("Menu não encontrado: " + itemDTO.menuId()));
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
                             StatusOrder statusOrder
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
                .build();
    }
}