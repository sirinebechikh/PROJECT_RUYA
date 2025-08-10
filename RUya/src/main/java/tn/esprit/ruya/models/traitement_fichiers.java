package tn.esprit.ruya.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TRAITEMENT")
public class traitement_fichiers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTraitement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fichier", nullable = false)
    private Fichier fichier;

    @Column(name = "path_reception", nullable = false)
    private String pathReception;

    @Column(name = "path_envoie", nullable = false)
    private String pathEnvoie;

    @Column(name = "date_traitement", nullable = false)
    private LocalDate dateTraitement;

    @Column(name = "nombre", nullable = false)
    private Integer nombre;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "statut", nullable = false)
    private String statut;

    // 👉 Ajouter cette méthode pour initialiser automatiquement dateTraitement si null
    @PrePersist
    public void prePersist() {
        if (this.dateTraitement == null) {
            this.dateTraitement = LocalDate.now();
        }
    }
}
