package br.com.apicomanda.domain;

import br.com.apicomanda.enums.StatusTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tables")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Tables {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long numberTable;
    private Long chairsAvailable;
    @Enumerated(EnumType.STRING)
    private StatusTable status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @JsonIgnore
    @ToString.Exclude
    private Admin admin;
}
