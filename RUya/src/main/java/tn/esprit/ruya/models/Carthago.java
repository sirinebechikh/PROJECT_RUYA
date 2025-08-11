package tn.esprit.ruya.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    // === VARIABLES EXISTANTES ===
    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER", nullable = false)
    private String typeFichier; // ELECTRONIQUE, MANUEL, WEB

    @Column(name = "NATURE_FICHIER", nullable = false)
    private String natureFichier; // FICHIER, REMISE

    @Column(name = "CODE_VALEUR", nullable = false)
    private String codeValeur; // CTR_RECU, BO_DINARS, AVANT_CTR, APRES_CTR, DOUBLE, NON_PARVENU, A_VERIFIER, TRAITE

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

    // Variables pour session du jour
    @Column(name = "SESSION_DATE")
    private LocalDate sessionDate; // date de la session de traitement

    @Column(name = "STATUT_CHEQUE")
    private String statutCheque; // TRAITE, EN_COURS, REJETE, PENDING

    @Column(name = "NUMERO_CHEQUE")
    private String numeroCheque;

    @Column(name = "BANQUE_EMETTRICE")
    private String banqueEmettrice;

    // Variables pour Carthago avant/après CTR
    @Column(name = "AVANT_CTR")
    private Boolean avantCTR; // true si avant traitement CTR

    @Column(name = "APRES_CTR")
    private Boolean apresCTR; // true si après traitement CTR

    @Column(name = "STATUT_IMAGE")
    private Integer statutImage; // 1, 2, 3 pour les différents statuts d'image

    @Column(name = "TRAITE_PAR_CTR")
    private Boolean traiteParCTR; // true si traité par CTR

    @Column(name = "DATE_TRAITEMENT_CTR")
    private LocalDateTime dateTraitementCTR;

    // Variables pour validation BO
    @Column(name = "VALIDE_BO_DINARS")
    private Boolean valideBodinars; // validation en dinars

    @Column(name = "CHEQUE_WEB_BO")
    private Boolean chequeWebBo; // chèque web validé BO

    // Variables pour remises en double
    @Column(name = "REMISE_DOUBLE")
    private Boolean remiseDouble; // détection doublons

    @Column(name = "REFERENCE_ORIGINALE")
    private String referenceOriginale; // référence du chèque original

    // Variables pour fichiers ENV
    @Column(name = "FICHIER_ENV")
    private Boolean fichierEnv; // fichier d'environnement

    @Column(name = "CODE_ENV")
    private String codeEnv; // code environnement

    // Variables de contrôle et vérification
    @Column(name = "A_VERIFIER")
    private Boolean aVerifier; // nécessite vérification manuelle

    @Column(name = "CONTROLE_EFFECTUE")
    private Boolean controleEffectue; // contrôle terminé

    @Column(name = "DATE_CONTROLE")
    private LocalDateTime dateControle;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
