package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Fichier;

import java.time.LocalDateTime;

@Repository
public interface FichierRepository extends JpaRepository<Fichier, Long> {

    // Comptage par période
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end")
    Integer countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Somme des montants par période
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Comptage par type de fichier
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.typeFichier = :typeFichier")
    Integer countByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("typeFichier") String typeFichier);

    // Somme par type de fichier
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.typeFichier = :typeFichier")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end,
                                                      @Param("typeFichier") String typeFichier);

    // Comptage par nature de fichier
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.natureFichier = :natureFichier")
    Integer countByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("natureFichier") String natureFichier);

    // Somme par nature de fichier
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.natureFichier = :natureFichier")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end,
                                                        @Param("natureFichier") String natureFichier);

    // Comptage par code valeur
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.codeValeur = :codeValeur")
    Integer countByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("codeValeur") String codeValeur);

    // Somme par code valeur
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.codeValeur = :codeValeur")
    Double sumMontantByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end,
                                                     @Param("codeValeur") String codeValeur);

    // Comptage par sens
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.sens = :sens")
    Integer countByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("sens") String sens);

    // Somme par sens
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.sens = :sens")
    Double sumMontantByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("sens") String sens);
}