package tn.esprit.ruya.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "CARTHAGO")
public class Carthago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CARTHAGO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER", nullable = false)
    private String typeFichier;

    @Column(name = "NATURE_FICHIER", nullable = false)
    private String natureFichier;

    @Column(name = "CODE_VALEUR", nullable = false)
    private String codeValeur;

    @Column(name = "COD_EN")
    private String codEn;

    @Column(name = "SENS")
    private String sens;

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NOMBER")
    private Integer nomber;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
