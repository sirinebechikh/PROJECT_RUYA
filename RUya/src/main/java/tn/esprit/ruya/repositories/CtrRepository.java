package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.CTR;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CtrRepository extends JpaRepository<CTR, Long> {

    // === MÉTHODES DE COMPTAGE ===
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    Long countByCreatedAtBetweenAndStatutCtr(LocalDateTime start, LocalDateTime end, String statutCtr);
    Long countByCreatedAtBetweenAndTypeOperation(LocalDateTime start, LocalDateTime end, String typeOperation);
    Long countByCreatedAtBetweenAndEquilibre(LocalDateTime start, LocalDateTime end, Boolean equilibre);
    Long countByCreatedAtBetweenAndRemiseDouble(LocalDateTime start, LocalDateTime end, Boolean remiseDouble);
    Long countByCreatedAtBetweenAndRemiseNonParvenue(LocalDateTime start, LocalDateTime end, Boolean remiseNonParvenue);

    // === MÉTHODES DE SOMME ===
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.statutCtr = :statut")
    Double sumMontantByCreatedAtBetweenAndStatutCtr(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statut") String statut);

    @Query("SELECT COALESCE(SUM(c.montantCarthago), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantCarthagoByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.montantFichiers), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantFichiersByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES SPÉCIFIQUES CTR ===
    @Query("SELECT COALESCE(SUM(c.nombreCarthago), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreCarthagoByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.nombreFichiers), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreFichiersByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.nombreElements), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreElementsByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === VÉRIFICATIONS D'ÉQUILIBRAGE ===
    @Query("SELECT COUNT(c) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.equilibre = true")
    Long countEquilibreByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.difference), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumDifferenceByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODE AJOUTÉE POUR RÉSOUDRE L'ERREUR ===
    List<CTR> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}