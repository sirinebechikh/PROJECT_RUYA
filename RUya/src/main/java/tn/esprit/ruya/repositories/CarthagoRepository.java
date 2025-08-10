// CarthagoRepository.java
package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Carthago;

import java.time.LocalDateTime;

@Repository
public interface CarthagoRepository extends JpaRepository<Carthago, Long> {

    // Comptage par période
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end")
    Integer countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Somme des montants par période
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Comptage par type de fichier
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :typeFichier")
    Integer countByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  @Param("typeFichier") String typeFichier);

    // Somme par type de fichier
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :typeFichier")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end,
                                                      @Param("typeFichier") String typeFichier);

    // Comptage par sens
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.sens = :sens")
    Integer countByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("sens") String sens);

    // Somme par sens
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.sens = :sens")
    Double sumMontantByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("sens") String sens);

    // Comptage par code valeur
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.codeValeur = :codeValeur")
    Integer countByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("codeValeur") String codeValeur);

    // Somme par code valeur
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.codeValeur = :codeValeur")
    Double sumMontantByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end,
                                                     @Param("codeValeur") String codeValeur);

    // Comptage par nature de fichier
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.natureFichier = :natureFichier")
    Integer countByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("natureFichier") String natureFichier);

    // Somme par nature de fichier
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.natureFichier = :natureFichier")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end,
                                                        @Param("natureFichier") String natureFichier);

    // Comptage par type et code valeur
    @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :typeFichier AND c.codeValeur = :codeValeur")
    Integer countByCreatedAtBetweenAndTypeFichierAndCodeValeur(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end,
                                                               @Param("typeFichier") String typeFichier,
                                                               @Param("codeValeur") String codeValeur);

    // Somme par type et code valeur
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :typeFichier AND c.codeValeur = :codeValeur")
    Double sumMontantByCreatedAtBetweenAndTypeFichierAndCodeValeur(@Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end,
                                                                   @Param("typeFichier") String typeFichier,
                                                                   @Param("codeValeur") String codeValeur);
}