// ====== CarthageRepository.java - Corrigé ======
package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Carthago;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface CarthageRepository extends JpaRepository<Carthago, Long> {

    // === MÉTHODES DE BASE ===
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES PAR TYPE_FICHIER ===
    Long countByCreatedAtBetweenAndTypeFichier(LocalDateTime start, LocalDateTime end, String typeFichier);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :type")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type);

    // === MÉTHODES PAR NATURE_FICHIER ===
    Long countByCreatedAtBetweenAndNatureFichier(LocalDateTime start, LocalDateTime end, String natureFichier);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.natureFichier = :nature")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("nature") String nature);

    // === MÉTHODES PAR SENS ===
    Long countByCreatedAtBetweenAndSens(LocalDateTime start, LocalDateTime end, String sens);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.sens = :sens")
    Double sumMontantByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("sens") String sens);

    // === MÉTHODES NOUVELLES VARIABLES ===

    // Session du jour
    Long countByCreatedAtBetweenAndSessionDate(LocalDateTime start, LocalDateTime end, LocalDate sessionDate);
    Long countByCreatedAtBetweenAndStatutCheque(LocalDateTime start, LocalDateTime end, String statutCheque);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.statutCheque = :statut")
    Double sumMontantByCreatedAtBetweenAndStatutCheque(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statut") String statut);

    // Avant/Après CTR
    Long countByCreatedAtBetweenAndAvantCTR(LocalDateTime start, LocalDateTime end, Boolean avantCTR);
    Long countByCreatedAtBetweenAndApresCTR(LocalDateTime start, LocalDateTime end, Boolean apresCTR);
    Long countByCreatedAtBetweenAndStatutImage(LocalDateTime start, LocalDateTime end, Integer statutImage);
    Long countByCreatedAtBetweenAndTraiteParCTR(LocalDateTime start, LocalDateTime end, Boolean traiteParCTR);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.avantCTR = :avant")
    Double sumMontantByCreatedAtBetweenAndAvantCTR(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("avant") Boolean avant);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.apresCTR = :apres")
    Double sumMontantByCreatedAtBetweenAndApresCTR(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("apres") Boolean apres);

    // Validation BO
    Long countByCreatedAtBetweenAndValideBodinars(LocalDateTime start, LocalDateTime end, Boolean valideBodinars);
    Long countByCreatedAtBetweenAndChequeWebBo(LocalDateTime start, LocalDateTime end, Boolean chequeWebBo);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.valideBodinars = :valide")
    Double sumMontantByCreatedAtBetweenAndValideBodinars(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("valide") Boolean valide);

    // Remises en double et ENV
    Long countByCreatedAtBetweenAndRemiseDouble(LocalDateTime start, LocalDateTime end, Boolean remiseDouble);
    Long countByCreatedAtBetweenAndFichierEnv(LocalDateTime start, LocalDateTime end, Boolean fichierEnv);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.remiseDouble = :double")
    Double sumMontantByCreatedAtBetweenAndRemiseDouble(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("double") Boolean remiseDouble);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.fichierEnv = :env")
    Double sumMontantByCreatedAtBetweenAndFichierEnv(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("env") Boolean env);

    // Contrôles et vérifications
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.aVerifier = :verifier")
    Long countByCreatedAtBetweenAndAVerifier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("verifier") Boolean verifier);

    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.controleEffectue = :controle")
    Long countByCreatedAtBetweenAndControleEffectue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("controle") Boolean controle);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.aVerifier = :verifier")
    Double sumMontantByCreatedAtBetweenAndAVerifier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("verifier") Boolean verifier);

    // === COMBINAISONS COMPLEXES ===
    Long countByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(LocalDateTime start, LocalDateTime end, String typeFichier, Boolean traiteParCTR);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :type AND c.traiteParCTR = :traite")
    Double sumMontantByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type, @Param("traite") Boolean traite);
}