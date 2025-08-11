// ====== FichierRepository.java - Corrigé ======
package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Fichier;

import java.time.LocalDateTime;

@Repository
public interface FichierRepository extends JpaRepository<Fichier, Long> {

    // === MÉTHODES DE BASE ===
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES EXISTANTES ===
    Long countByCreatedAtBetweenAndTypeFichier(LocalDateTime start, LocalDateTime end, String typeFichier);
    Long countByCreatedAtBetweenAndNatureFichier(LocalDateTime start, LocalDateTime end, String natureFichier);
    Long countByCreatedAtBetweenAndCodeValeur(LocalDateTime start, LocalDateTime end, String codeValeur);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.typeFichier = :type")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.natureFichier = :nature")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("nature") String nature);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.codeValeur = :code")
    Double sumMontantByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("code") String code);

    // === NOUVELLES MÉTHODES DASHBOARD ===

    // Client Externe
    Long countByCreatedAtBetweenAndStatutRemise(LocalDateTime start, LocalDateTime end, String statutRemise);
    Long countByCreatedAtBetweenAndOrigineSaisie(LocalDateTime start, LocalDateTime end, String origineSaisie);
    Long countByCreatedAtBetweenAndClientExterneIdIsNotNull(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.statutRemise = :statut")
    Double sumMontantByCreatedAtBetweenAndStatutRemise(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statut") String statut);

    // F_GENERER par Encaisse
    Long countByCreatedAtBetweenAndGenereParEncaisse(LocalDateTime start, LocalDateTime end, Boolean genereParEncaisse);
    Long countByCreatedAtBetweenAndTypeEncaissement(LocalDateTime start, LocalDateTime end, String typeEncaissement);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.genereParEncaisse = :genere")
    Double sumMontantByCreatedAtBetweenAndGenereParEncaisse(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    // Validation BO
    Long countByCreatedAtBetweenAndValidationBO(LocalDateTime start, LocalDateTime end, Boolean validationBO);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validationBO = :validation")
    Double sumMontantByCreatedAtBetweenAndValidationBO(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("validation") Boolean validation);

    // === COMBINAISONS COMPLEXES ===
    Long countByCreatedAtBetweenAndTypeFichierAndOrigineSaisie(LocalDateTime start, LocalDateTime end, String typeFichier, String origineSaisie);
    Long countByCreatedAtBetweenAndValidationBOAndCodeValeur(LocalDateTime start, LocalDateTime end, Boolean validationBO, String codeValeur);
}
