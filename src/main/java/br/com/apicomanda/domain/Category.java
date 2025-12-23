package br.com.apicomanda.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
