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
@Table(name = "FICHIERS")
public class Fichier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FICHIER")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER")
    private String typeFichier;

    @Column(name = "NATURE_FICHIER")
    private String natureFichier;

    @Column(name = "CODE_VALEUR")
    private String codeValeur;

    @Column(name = "COD_EN")
    private String codEn;

    @Column(name = "SENS")
    private String sens;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NOMBER")
    private Integer nomber;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}