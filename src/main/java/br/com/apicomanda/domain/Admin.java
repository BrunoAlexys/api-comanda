package br.com.apicomanda.domain;

import br.com.apicomanda.dto.admin.CreateAdminRequest;
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
@Table(name = "admins")
public class Admin {
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
    @JoinTable(name = "admin_profile", joinColumns = @JoinColumn(name = "admin_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id", referencedColumnName = "id"))
    private List<Profile> profiles;
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Fee> fees = new ArrayList<>();
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Order> orders = new ArrayList<>();
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Category> categories = new ArrayList<>();

    public static Admin toEntity(CreateAdminRequest userDTO, String encryptedPassword, Profile profile) {
        return Admin.builder()
                .name(userDTO.name())
                .email(userDTO.email())
                .telephone(userDTO.telephone())
                .password(encryptedPassword)
                .profiles(List.of(profile))
                .status(StatusUser.ENABLED.getStatusValue())
                .build();
    }
}
