package br.com.apicomanda.domain;

import br.com.apicomanda.enums.StatusOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int tableNumber;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderFee> appliedFees;
    private String additionalComment;
    private BigDecimal totalOrderPrice;
    private BigDecimal totalFeesValue;
    private BigDecimal finalTotalPrice;
    @Enumerated(EnumType.STRING)
    private StatusOrder statusOrder;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
