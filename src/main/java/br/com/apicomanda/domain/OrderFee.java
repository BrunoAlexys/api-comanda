package br.com.apicomanda.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_fees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal amount;
}