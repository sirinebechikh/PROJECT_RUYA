package tn.esprit.ruya.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.*;
import tn.esprit.ruya.repositories.CarthageRepository;
import tn.esprit.ruya.repositories.FichierRepository;
import tn.esprit.ruya.repositories.CtrRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private CarthageRepository carthagoRepository;

    @Autowired
    private FichierRepository fichierRepository;

    @Autowired
    private CtrRepository ctrRepository;

    public DashboardResponseDTO getDashboardDataCorrected() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).minusSeconds(1);

        return getDashboardDataForPeriodCorrected(today, endOfDay);
    }

    public DashboardResponseDTO getDashboardDataForPeriodCorrected(LocalDateTime start, LocalDateTime end) {
        DashboardResponseDTO response = new DashboardResponseDTO();
        List<CardDataDTO> cardData = new ArrayList<>();

        // Cartes corrigées selon le flux métier réel
        cardData.add(buildCreationFichiersCard(start, end));
        cardData.add(buildValidationFichiersCard(start, end));
        cardData.add(buildFluxFichiersCarthago(start, end));
        cardData.add(buildTraitementCarthagoCard(start, end));
        cardData.add(buildFluxCarthagoCTRCard(start, end));
        cardData.add(buildEquilibrageGlobalCard(start, end));

        response.setCardData(cardData);
        response.setGlobalStats(buildGlobalStats(start, end));
        return response;
    }

    /**
     * CRÉATION FICHIERS - Fichiers créés dans le système
     */
    private CardDataDTO buildCreationFichiersCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("CRÉATION FICHIERS");
        card.setIcon("fas fa-file-plus");
        card.setType("info");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Total fichiers créés
            Long totalFichiers = fichierRepository.countByCreatedAtBetween(start, end);
            Double montantTotal = fichierRepository.sumMontantByCreatedAtBetween(start, end);
            data.add(new DataRowDTO("Total fichiers créés", safeIntValue(totalFichiers),
                    formatMontant(montantTotal), "info"));

            // Remises créées
            Long remisesCreees = fichierRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            Double montantRemises = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            data.add(new DataRowDTO("Remises créées", safeIntValue(remisesCreees),
                    formatMontant(montantRemises), null));

            // Par origine
            Long remisesWeb = fichierRepository.countByCreatedAtBetweenAndOrigineSaisie(start, end, "WEB");
            data.add(new DataRowDTO("Remises WEB", safeIntValue(remisesWeb), null, null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * VALIDATION FICHIERS - Fichiers validés par BO
     */
    private CardDataDTO buildValidationFichiersCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("VALIDATION FICHIERS");
        card.setIcon("fas fa-check-circle");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Fichiers validés
            Long fichiersValides = fichierRepository.countByCreatedAtBetweenAndValidation(start, end, true);
            Double montantValides = fichierRepository.sumMontantByCreatedAtBetweenAndValidation(start, end, true);
            data.add(new DataRowDTO("Fichiers validés", safeIntValue(fichiersValides),
                    formatMontant(montantValides), "success"));

            // Fichiers en attente
            Long fichiersAttente = fichierRepository.countByCreatedAtBetweenAndValidation(start, end, false);
            data.add(new DataRowDTO("En attente validation", safeIntValue(fichiersAttente), null, "warning"));

            // Taux de validation
            Long totalFichiers = fichierRepository.countByCreatedAtBetween(start, end);
            Double tauxValidation = totalFichiers > 0 ? (fichiersValides * 100.0) / totalFichiers : 0.0;
            data.add(new DataRowDTO("Taux validation", String.format("%.1f%%", tauxValidation), null,
                    tauxValidation > 90 ? "success" : "warning"));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * FLUX FICHIERS → CARTHAGO - Vérification de la transmission
     */
    private CardDataDTO buildFluxFichiersCarthago(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("FLUX FICHIERS → CARTHAGO");
        card.setIcon("fas fa-arrow-right");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Fichiers validés (doivent être transmis)
            Long fichiersValides = fichierRepository.countByCreatedAtBetweenAndValidation(start, end, true);
            Double montantFichiersValides = fichierRepository.sumMontantByCreatedAtBetweenAndValidation(start, end, true);

            // Éléments reçus dans Carthago
            Long elementsRecus = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            Double montantRecus = carthagoRepository.sumMontantByCreatedAtBetweenAndSens(start, end, "ENTRANT");

            data.add(new DataRowDTO("Fichiers validés (à transmettre)", safeIntValue(fichiersValides),
                    formatMontant(montantFichiersValides), null));

            data.add(new DataRowDTO("Éléments reçus Carthago", safeIntValue(elementsRecus),
                    formatMontant(montantRecus), null));

            // Vérification cohérence
            Long ecartNombre = fichiersValides - elementsRecus;
            Double ecartMontant = montantFichiersValides - montantRecus;

            String statusCohérence = (Math.abs(ecartNombre) <= 1 && Math.abs(ecartMontant) < 1.0) ? "success" : "warning";
            data.add(new DataRowDTO("Cohérence transmission",
                    "Écart: " + ecartNombre + " fichiers",
                    "Écart: " + formatMontant(ecartMontant), statusCohérence));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * TRAITEMENT CARTHAGO - Éléments traités par Carthago
     */
    private CardDataDTO buildTraitementCarthagoCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("TRAITEMENT CARTHAGO");
        card.setIcon("fas fa-server");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Total éléments Carthago
            Long totalCarthago = carthagoRepository.countByCreatedAtBetween(start, end);
            Double montantTotal = carthagoRepository.sumMontantByCreatedAtBetween(start, end);

            // Éléments traités
            Long elementsTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantTraites = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");

            // Éléments à vérifier
            Long aVerifier = carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true);

            data.add(new DataRowDTO("Total Carthago", safeIntValue(totalCarthago),
                    formatMontant(montantTotal), "info"));

            data.add(new DataRowDTO("Éléments traités", safeIntValue(elementsTraites),
                    formatMontant(montantTraites), "success"));

            data.add(new DataRowDTO("À vérifier", safeIntValue(aVerifier), null,
                    aVerifier > 10 ? "warning" : "info"));

            // Taux de traitement
            Double tauxTraitement = totalCarthago > 0 ? (elementsTraites * 100.0) / totalCarthago : 0.0;
            data.add(new DataRowDTO("Taux traitement", String.format("%.1f%%", tauxTraitement), null,
                    tauxTraitement > 90 ? "success" : "warning"));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * FLUX CARTHAGO → CTR - Vérification transmission vers CTR
     */
    private CardDataDTO buildFluxCarthagoCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("FLUX CARTHAGO → CTR");
        card.setIcon("fas fa-exchange-alt");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Carthago traités (doivent être envoyés vers CTR)
            Long carthagoTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantCarthagoTraites = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");

            // CTR reçus
            Long ctrRecus = ctrRepository.countByCreatedAtBetween(start, end);
            Double montantCTR = ctrRepository.sumMontantByCreatedAtBetween(start, end);

            data.add(new DataRowDTO("Carthago traités (à envoyer)", safeIntValue(carthagoTraites),
                    formatMontant(montantCarthagoTraites), null));

            data.add(new DataRowDTO("CTR reçus", safeIntValue(ctrRecus),
                    formatMontant(montantCTR), null));

            // Vérification équilibrage
            boolean equilibreNombre = carthagoTraites.equals(ctrRecus);
            boolean equilibreMontant = Math.abs(montantCarthagoTraites - montantCTR) < 1.0;

            String statusEquilibrage = (equilibreNombre && equilibreMontant) ? "success" : "warning";
            String messageEquilibrage = equilibreNombre && equilibreMontant ? "Équilibré" : "Déséquilibré";

            data.add(new DataRowDTO("État équilibrage", messageEquilibrage, null, statusEquilibrage));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * ÉQUILIBRAGE GLOBAL - Vue d'ensemble des flux
     */
    private CardDataDTO buildEquilibrageGlobalCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("ÉQUILIBRAGE GLOBAL");
        card.setIcon("fas fa-balance-scale");
        card.setType("warning");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            ResultatEquilibrageDTO equilibrage = calculerEquilibrageGlobalCorrect(start, end);

            // Flux Fichiers → Carthago
            data.add(new DataRowDTO("Flux Fichiers → Carthago",
                    equilibrage.isCoherenceFichierCarthago() ? "Cohérent" : "Incohérent",
                    "Écart: " + equilibrage.getEcartFichierCarthago(),
                    equilibrage.isCoherenceFichierCarthago() ? "success" : "danger"));

            // Flux Carthago → CTR
            data.add(new DataRowDTO("Flux Carthago → CTR",
                    equilibrage.isCoherenceCarthagoCTR() ? "Cohérent" : "Incohérent",
                    "Écart: " + equilibrage.getEcartCarthagoCTR(),
                    equilibrage.isCoherenceCarthagoCTR() ? "success" : "danger"));

            // État global
            boolean equilibreGlobal = equilibrage.isCoherenceFichierCarthago() && equilibrage.isCoherenceCarthagoCTR();
            data.add(new DataRowDTO("État global",
                    equilibreGlobal ? "Système équilibré" : "Déséquilibres détectés",
                    null, equilibreGlobal ? "success" : "danger"));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Impossible de calculer l'équilibrage", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * CALCUL D'ÉQUILIBRAGE GLOBAL CORRIGÉ
     */
    public ResultatEquilibrageDTO calculerEquilibrageGlobalCorrect(LocalDateTime start, LocalDateTime end) {
        ResultatEquilibrageDTO resultat = new ResultatEquilibrageDTO();

        try {
            // Données Fichiers
            Long fichiersValides = fichierRepository.countByCreatedAtBetweenAndValidation(start, end, true);
            Double montantFichiers = fichierRepository.sumMontantByCreatedAtBetweenAndValidation(start, end, true);

            // Données Carthago
            Long carthagoRecus = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            Long carthagoTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantCarthagoTraites = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");

            // Données CTR
            Long ctrRecus = ctrRepository.countByCreatedAtBetween(start, end);
            Double montantCTR = ctrRepository.sumMontantByCreatedAtBetween(start, end);

            // Calculs de cohérence
            resultat.setNombreFichiersValides(fichiersValides);
            resultat.setNombreCarthagoRecus(carthagoRecus);
            resultat.setNombreCarthagoTraites(carthagoTraites);
            resultat.setNombreCTRRecus(ctrRecus);

            resultat.setMontantFichiers(montantFichiers);
            resultat.setMontantCarthagoTraites(montantCarthagoTraites);
            resultat.setMontantCTR(montantCTR);

            // Cohérence Fichiers → Carthago (tolérance de ±1)
            resultat.setEcartFichierCarthago(fichiersValides - carthagoRecus);
            resultat.setCoherenceFichierCarthago(Math.abs(resultat.getEcartFichierCarthago()) <= 1);

            // Cohérence Carthago → CTR
            resultat.setEcartCarthagoCTR(carthagoTraites - ctrRecus);
            resultat.setCoherenceCarthagoCTR(Math.abs(resultat.getEcartCarthagoCTR()) <= 1);

            // Cohérence montants (tolérance de 1 DT)
            resultat.setEcartMontantGlobal(montantFichiers - montantCTR);
            resultat.setCoherenceMontants(Math.abs(resultat.getEcartMontantGlobal()) < 1.0);

        } catch (Exception e) {
            // Valeurs par défaut en cas d'erreur
            resultat = new ResultatEquilibrageDTO();
        }

        return resultat;
    }

    /**
     * Construction des statistiques globales pour le dashboard
     */
    private List<StatCardDTO> buildGlobalStats(LocalDateTime start, LocalDateTime end) {
        List<StatCardDTO> globalStats = new ArrayList<>();

        try {
            // Total des remises
            Long totalRemises = fichierRepository.countByCreatedAtBetween(start, end);
            Double montantTotalRemises = fichierRepository.sumMontantByCreatedAtBetween(start, end);
            globalStats.add(new StatCardDTO(
                String.valueOf(totalRemises), 
                "Total Remises", 
                formatMontant(montantTotalRemises)
            ));

            // Total des éléments traités
            Long totalElementsTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantElementsTraites = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            globalStats.add(new StatCardDTO(
                String.valueOf(totalElementsTraites), 
                "Éléments Traités", 
                formatMontant(montantElementsTraites)
            ));

            // Taux de réussite global
            Long fichiersValides = fichierRepository.countByCreatedAtBetweenAndValidation(start, end, true);
            Long totalFichiers = fichierRepository.countByCreatedAtBetween(start, end);
            Double tauxReussite = totalFichiers > 0 ? (fichiersValides * 100.0) / totalFichiers : 0.0;
            String statusTaux = tauxReussite > 90 ? "Excellent" : tauxReussite > 70 ? "Bon" : "À améliorer";
            globalStats.add(new StatCardDTO(
                String.format("%.1f%%", tauxReussite), 
                "Taux de Validation", 
                null, 
                statusTaux
            ));

            // Dernière mise à jour
            globalStats.add(new StatCardDTO(
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 
                "Dernière MAJ", 
                null, 
                null
            ));

            // État global du système
            ResultatEquilibrageDTO equilibrage = calculerEquilibrageGlobalCorrect(start, end);
            boolean systemeEquilibre = equilibrage.isCoherenceFichierCarthago() && 
                                     equilibrage.isCoherenceCarthagoCTR() && 
                                     equilibrage.isCoherenceMontants();
            globalStats.add(new StatCardDTO(
                systemeEquilibre ? "OK" : "ALERTE", 
                "État Système", 
                null, 
                systemeEquilibre ? "success" : "warning"
            ));

            // Total CTR reçus
            Long totalCTR = ctrRepository.countByCreatedAtBetween(start, end);
            Double montantCTR = ctrRepository.sumMontantByCreatedAtBetween(start, end);
            globalStats.add(new StatCardDTO(
                String.valueOf(totalCTR), 
                "Total CTR", 
                formatMontant(montantCTR)
            ));

        } catch (Exception e) {
            // En cas d'erreur, ajouter une stat d'erreur
            globalStats.add(new StatCardDTO(
                "ERROR", 
                "Erreur Calcul", 
                null, 
                "danger"
            ));
        }

        return globalStats;
    }

    // === MÉTHODES UTILITAIRES ===

    private int safeIntValue(Long value) {
        return value != null ? value.intValue() : 0;
    }

    private String formatMontant(Double montant) {
        if (montant == null || montant == 0.0) {
            return "0 DT";
        }
        return String.format("%.2f DT", montant);
    }

    // Classe pour le résultat d'équilibrage
    public static class ResultatEquilibrageDTO {
        private Long nombreFichiersValides = 0L;
        private Long nombreCarthagoRecus = 0L;
        private Long nombreCarthagoTraites = 0L;
        private Long nombreCTRRecus = 0L;

        private Double montantFichiers = 0.0;
        private Double montantCarthagoTraites = 0.0;
        private Double montantCTR = 0.0;

        private Long ecartFichierCarthago = 0L;
        private Long ecartCarthagoCTR = 0L;
        private Double ecartMontantGlobal = 0.0;

        private boolean coherenceFichierCarthago = true;
        private boolean coherenceCarthagoCTR = true;
        private boolean coherenceMontants = true;

        // Getters et setters...
        public Long getNombreFichiersValides() { return nombreFichiersValides; }
        public void setNombreFichiersValides(Long nombreFichiersValides) { this.nombreFichiersValides = nombreFichiersValides; }

        public Long getNombreCarthagoRecus() { return nombreCarthagoRecus; }
        public void setNombreCarthagoRecus(Long nombreCarthagoRecus) { this.nombreCarthagoRecus = nombreCarthagoRecus; }

        public Long getNombreCarthagoTraites() { return nombreCarthagoTraites; }
        public void setNombreCarthagoTraites(Long nombreCarthagoTraites) { this.nombreCarthagoTraites = nombreCarthagoTraites; }

        public Long getNombreCTRRecus() { return nombreCTRRecus; }
        public void setNombreCTRRecus(Long nombreCTRRecus) { this.nombreCTRRecus = nombreCTRRecus; }

        public Double getMontantFichiers() { return montantFichiers; }
        public void setMontantFichiers(Double montantFichiers) { this.montantFichiers = montantFichiers; }

        public Double getMontantCarthagoTraites() { return montantCarthagoTraites; }
        public void setMontantCarthagoTraites(Double montantCarthagoTraites) { this.montantCarthagoTraites = montantCarthagoTraites; }

        public Double getMontantCTR() { return montantCTR; }
        public void setMontantCTR(Double montantCTR) { this.montantCTR = montantCTR; }

        public Long getEcartFichierCarthago() { return ecartFichierCarthago; }
        public void setEcartFichierCarthago(Long ecartFichierCarthago) { this.ecartFichierCarthago = ecartFichierCarthago; }

        public Long getEcartCarthagoCTR() { return ecartCarthagoCTR; }
        public void setEcartCarthagoCTR(Long ecartCarthagoCTR) { this.ecartCarthagoCTR = ecartCarthagoCTR; }

        public Double getEcartMontantGlobal() { return ecartMontantGlobal; }
        public void setEcartMontantGlobal(Double ecartMontantGlobal) { this.ecartMontantGlobal = ecartMontantGlobal; }

        public boolean isCoherenceFichierCarthago() { return coherenceFichierCarthago; }
        public void setCoherenceFichierCarthago(boolean coherenceFichierCarthago) { this.coherenceFichierCarthago = coherenceFichierCarthago; }

        public boolean isCoherenceCarthagoCTR() { return coherenceCarthagoCTR; }
        public void setCoherenceCarthagoCTR(boolean coherenceCarthagoCTR) { this.coherenceCarthagoCTR = coherenceCarthagoCTR; }

        public boolean isCoherenceMontants() { return coherenceMontants; }
        public void setCoherenceMontants(boolean coherenceMontants) { this.coherenceMontants = coherenceMontants; }
    }
}