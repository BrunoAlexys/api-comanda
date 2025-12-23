package br.com.apicomanda.domain;

import br.com.apicomanda.dto.user.CreateUserRequest;
import br.com.apicomanda.enums.StatusUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "email", length = 100, nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "telephone", length = 20, nullable = false)
    private String telephone;
    @Column(name = "status")
    private boolean status;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "user_profile", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id", referencedColumnName = "id"))
    private List<Profile> profiles;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Fee> fees = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Order> orders = new ArrayList<>();

    public static User toEntity(CreateUserRequest userDTO, String encryptedPassword,Profile profile) {
        return User.builder()
                .name(userDTO.name())
                .email(userDTO.email())
                .telephone(userDTO.telephone())
                .password(encryptedPassword)
                .profiles(List.of(profile))
                .status(StatusUser.ENABLED.getStatusValue())
                .build();
    }
}
