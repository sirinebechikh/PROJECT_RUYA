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

    // === VARIABLES EXISTANTES ===
    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER")
    private String typeFichier; // WEB, ELECTRONIQUE, MANUEL, EN_SAISIE

    @Column(name = "NATURE_FICHIER")
    private String natureFichier; // REMISE, FICHIER

    @Column(name = "CODE_VALEUR")
    private String codeValeur; // BO_VALIDE, APRES_CTR, etc.

    @Column(name = "COD_EN")
    private String codEn;

    @Column(name = "SENS")
    private String sens; // ENTRANT, SORTANT

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NOMBER")
    private Integer nomber;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // === NOUVELLES VARIABLES POUR DASHBOARD ===

    // Variables spécifiques aux remises
    @Column(name = "STATUT_REMISE")
    private String statutRemise; // CREE, VALIDE, TRAITE, REFUSE

    @Column(name = "ORIGINE_SAISIE")
    private String origineSaisie; // WEB, AGENCE, BATCH

    @Column(name = "VALIDATION_BO")
    private Boolean validationBO; // true/false pour validation back office

    @Column(name = "DATE_VALIDATION")
    private LocalDateTime dateValidation;

    // Variables pour le suivi F_GENERER
    @Column(name = "GENERE_PAR_ENCAISSE")
    private Boolean genereParEncaisse; // true si généré par encaisse

    @Column(name = "NUMERO_REMISE")
    private String numeroRemise; // numéro unique de remise

    @Column(name = "TYPE_ENCAISSEMENT")
    private String typeEncaissement; // IMMEDIAT, DIFFERE

    // Variables pour client externe
    @Column(name = "CLIENT_EXTERNE_ID")
    private String clientExterneId;

    @Column(name = "SESSION_ID")
    private String sessionId; // pour lier à une session

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}