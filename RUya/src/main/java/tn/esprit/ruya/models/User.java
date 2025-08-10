package tn.esprit.ruya.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    private String username;
    private String email;
    @Enumerated(EnumType.STRING)
    private RoleUser role;
    private String password;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist()
    {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpate() {
        this.updatedAt = LocalDateTime.now();
    }
}
