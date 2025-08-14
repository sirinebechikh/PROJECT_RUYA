// ====== FichierRepository.java - Corrigé avec toutes les méthodes ======
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

    // === MÉTHODES PAR TYPE/NATURE/CODE ===
    Long countByCreatedAtBetweenAndTypeFichier(LocalDateTime start, LocalDateTime end, String typeFichier);
    Long countByCreatedAtBetweenAndNatureFichier(LocalDateTime start, LocalDateTime end, String natureFichier);
    Long countByCreatedAtBetweenAndCodeValeur(LocalDateTime start, LocalDateTime end, String codeValeur);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.typeFichier = :type")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("type") String type);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.natureFichier = :nature")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("nature") String nature);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.codeValeur = :code")
    Double sumMontantByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("code") String code);

    // === MÉTHODES ORIGINE SAISIE ===
    Long countByCreatedAtBetweenAndOrigineSaisie(LocalDateTime start, LocalDateTime end, String origineSaisie);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.origineSaisie = :origine")
    Double sumMontantByCreatedAtBetweenAndOrigineSaisie(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("origine") String origine);

    // === MÉTHODES GÉNÉRÉ PAR ENCAISSE ===
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.genereParEncaisse = :genere")
    Long countByCreatedAtBetweenAndGenereParEncaisse(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.genereParEncaisse = :genere")
    Double sumMontantByCreatedAtBetweenAndGenereParEncaisse(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    // === MÉTHODES VALIDATION BO ===
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validationBO = :validation")
    Long countByCreatedAtBetweenAndValidationBO(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("validation") Boolean validation);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validationBO = :validation")
    Double sumMontantByCreatedAtBetweenAndValidationBO(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("validation") Boolean validation);

    // === MÉTHODES STATUT REMISE ===
    Long countByCreatedAtBetweenAndStatutRemise(LocalDateTime start, LocalDateTime end, String statutRemise);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.statutRemise = :statut")
    Double sumMontantByCreatedAtBetweenAndStatutRemise(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statut") String statut);

    // === MÉTHODES CLIENT EXTERNE ===
    Long countByCreatedAtBetweenAndClientExterneIdIsNotNull(LocalDateTime start, LocalDateTime end);
    Long countByCreatedAtBetweenAndTypeEncaissement(LocalDateTime start, LocalDateTime end, String typeEncaissement);

    // === *** NOUVELLES MÉTHODES POUR DASHBOARD SERVICE *** ===

    // Fichiers générés ET validés
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.genereParEncaisse = :genere AND f.validationBO = :valide")
    Long countByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.genereParEncaisse = :genere AND f.validationBO = :valide")
    Double sumMontantByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    // Remises générées ET validées
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.genereParEncaisse = :genere AND f.validationBO = :valide")
    Long countByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") String nature,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.genereParEncaisse = :genere AND f.validationBO = :valide")
    Double sumMontantByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") String nature,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    // Nature + Code Valeur
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.codeValeur = :code")
    Long countByCreatedAtBetweenAndNatureFichierAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") String nature,
            @Param("code") String code);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.codeValeur = :code")
    Double sumMontantByCreatedAtBetweenAndNatureFichierAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") String nature,
            @Param("code") String code);

    // === COMBINAISONS COMPLEXES EXISTANTES ===
    Long countByCreatedAtBetweenAndTypeFichierAndOrigineSaisie(LocalDateTime start, LocalDateTime end, String typeFichier, String origineSaisie);
    Long countByCreatedAtBetweenAndValidationBOAndCodeValeur(LocalDateTime start, LocalDateTime end, Boolean validationBO, String codeValeur);
}