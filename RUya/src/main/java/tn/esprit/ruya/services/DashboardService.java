package tn.esprit.ruya.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.*;
import tn.esprit.ruya.repositories.CarthageRepository;
import tn.esprit.ruya.repositories.FichierRepository;
import tn.esprit.ruya.repositories.CtrRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public DashboardResponseDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        return getDashboardDataForPeriod(startOfDay, endOfDay);
    }

    public DashboardResponseDTO getDashboardDataForPeriod(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        DashboardResponseDTO response = new DashboardResponseDTO();
        List<CardDataDTO> cardData = new ArrayList<>();

        // Construction des cartes avec calculs corrigés selon vos besoins
        cardData.add(buildEncaisseValeurCardCorrected(startOfDay, endOfDay));
        cardData.add(buildFichierGenererCardCorrected(startOfDay, endOfDay));
        cardData.add(buildCarthagoCardCorrected(startOfDay, endOfDay));
        cardData.add(buildCarthagoAvantCTRCardCorrected(startOfDay, endOfDay));
        cardData.add(buildCTRCardCorrected(startOfDay, endOfDay));
        cardData.add(buildActionsControlesCardCorrected(startOfDay, endOfDay));

        response.setCardData(cardData);
        return response;
    }

    /**
     * ENCAISSE VALEUR - Affiche les fichiers créés et validés
     */
    private CardDataDTO buildEncaisseValeurCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("ENCAISSE VALEUR");
        card.setIcon("fas fa-globe");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Fichiers créés et validés dans la table FICHIERS
            Long fichiersValides = fichierRepository.countByCreatedAtBetweenAndValidationBO(start, end, true);
            Double montantFichiersValides = fichierRepository.sumMontantByCreatedAtBetweenAndValidationBO(start, end, true);
            data.add(new DataRowDTO("Fichiers validés", safeIntValue(fichiersValides),
                    formatMontant(montantFichiersValides), "success"));

            // Remises créées
            Long remisesCreees = fichierRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            Double montantRemises = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            data.add(new DataRowDTO("Remises créées", safeIntValue(remisesCreees),
                    formatMontant(montantRemises), null));


        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * FICHIERS GENERER - Affiche les fichiers générés ET validés par encaisse
     */
    private CardDataDTO buildFichierGenererCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("FICHIERS GENERER par Encaisse");
        card.setIcon("fas fa-cogs");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises générées et validées
            Long remisesGenereesValides = fichierRepository.countByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
                    start, end, "REMISE", true, true);
            Double montantRemisesGenereesValides = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
                    start, end, "REMISE", true, true);
            data.add(new DataRowDTO("Remises générées validées", safeIntValue(remisesGenereesValides),
                    formatMontant(montantRemisesGenereesValides), "success"));
            // Fichiers générés ET validés (les deux conditions)
            Long fichiersGeneresetValides = fichierRepository.countByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
                    start, end, true, true);
            Double montantFichiersGeneresetValides = fichierRepository.sumMontantByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
                    start, end, true, true);
            data.add(new DataRowDTO("Fichiers générés et validés", safeIntValue(fichiersGeneresetValides),
                    formatMontant(montantFichiersGeneresetValides), "success"));




        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * CARTHAGO - Affiche les fichiers générés ET validés dans Carthago
     */
    private CardDataDTO buildCarthagoCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Fichiers Carthago");
        card.setIcon("fas fa-server");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Fichiers générés et validés dans Carthago (statut TRAITE = validé)
            Long fichiersCarthagoValides = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantCarthagoValides = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            data.add(new DataRowDTO("Fichiers générés et validés", safeIntValue(fichiersCarthagoValides),
                    formatMontant(montantCarthagoValides), "success"));

            // Fichiers consommés par Carthago (tous entrants)
            Long fichiersCarthago = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            Double montantCarthago = carthagoRepository.sumMontantByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            data.add(new DataRowDTO("Total consommés Carthago", safeIntValue(fichiersCarthago),
                    formatMontant(montantCarthago), null));

            // Générés vers CTR et validés
            Long generesCTRValides = carthagoRepository.countByCreatedAtBetweenAndTraiteParCTRAndStatutCheque(
                    start, end, true, "TRAITE");
            Double montantGeneresCTRValides = carthagoRepository.sumMontantByCreatedAtBetweenAndTraiteParCTRAndStatutCheque(
                    start, end, true, "TRAITE");
            data.add(new DataRowDTO("Générés vers CTR validés", safeIntValue(generesCTRValides),
                    formatMontant(montantGeneresCTRValides), "success"));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * CARTHAGO AVANT CTR - Affiche les données avant traitement CTR
     */
    private CardDataDTO buildCarthagoAvantCTRCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Carthago avant CTR");
        card.setIcon("fas fa-exchange-alt");
        card.setType("warning");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises avant CTR
            Long remisesAvantCTR = carthagoRepository.countByCreatedAtBetweenAndAvantCTRAndNatureFichier(
                    start, end, true, "REMISE");
            Double montantRemisesAvantCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndAvantCTRAndNatureFichier(
                    start, end, true, "REMISE");
            data.add(new DataRowDTO("Remises avant CTR", safeIntValue(remisesAvantCTR),
                    formatMontant(montantRemisesAvantCTR), null));

            // Chèques fichier avant CTR
            Long chequesFichierAvantCTR = carthagoRepository.countByCreatedAtBetweenAndNatureFichierAndAvantCTR(
                    start, end, "FICHIER", true);
            Double montantChequesFichier = carthagoRepository.sumMontantByCreatedAtBetweenAndNatureFichierAndAvantCTR(
                    start, end, "FICHIER", true);
            data.add(new DataRowDTO("Chèques fichier avant CTR", safeIntValue(chequesFichierAvantCTR),
                    formatMontant(montantChequesFichier), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * CTR - Affiche chèques et remises séparément (car chaque fichier = une remise)
     */
    private CardDataDTO buildCTRCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("CTR");
        card.setIcon("fas fa-copy");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {

            // Remises CTR (car chaque fichier = une remise)
            // Comptage des fichiers uniques qui correspondent à des remises
            Long remisesCTR = fichierRepository.countByCreatedAtBetweenAndNatureFichierAndCodeValeur(
                    start, end, "REMISE", "APRES_CTR");
            Double montantRemisesCTR = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichierAndCodeValeur(
                    start, end, "REMISE", "APRES_CTR");
            data.add(new DataRowDTO("Remises traitées", safeIntValue(remisesCTR),
                    formatMontant(montantRemisesCTR), null));
            // Chèques CTR (éléments individuels)
            Long chequesCTR = carthagoRepository.countByCreatedAtBetweenAndTraiteParCTR(start, end, true);
            Double montantChequesCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndTraiteParCTR(start, end, true);
            data.add(new DataRowDTO("Chèques", safeIntValue(chequesCTR),
                    formatMontant(montantChequesCTR), null));

            // Ensemble de fichiers formant des remises (regroupement)
            Long ensembleFichiersRemises = carthagoRepository.countDistinctRemisesByCreatedAtBetweenAndApresCTR(
                    start, end, true);
            data.add(new DataRowDTO("Ensembles fichiers/remises", safeIntValue(ensembleFichiersRemises),
                    null, null));

            // Fichiers ENV CTR
            Long fichiersEnvCTR = carthagoRepository.countByCreatedAtBetweenAndFichierEnvAndApresCTR(
                    start, end, true, true);
            Double montantFichiersEnv = carthagoRepository.sumMontantByCreatedAtBetweenAndFichierEnvAndApresCTR(
                    start, end, true, true);
            data.add(new DataRowDTO("Chèques fichier ENV", safeIntValue(fichiersEnvCTR),
                    formatMontant(montantFichiersEnv), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * ACTIONS ET CONTROLES - Équilibrage avec somme Fichiers + Carthago
     */
    private CardDataDTO buildActionsControlesCardCorrected(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Actions et Contrôles");
        card.setIcon("fas fa-tools");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Calcul d'équilibrage : (Fichiers + Carthago) vs CTR
            EquilibrageResultDTO equilibrage = calculerEquilibrageCorrect(start, end);

            Long sommeFichiersCarthago = equilibrage.getTotalCarFich();
            Long totalCTR = equilibrage.getNombreCTR();
            Long difference = sommeFichiersCarthago - totalCTR;

            // Affichage du résultat d'équilibrage
            String statusEquilibrage;
            String valueEquilibrage;

            if (difference == 0) {
                statusEquilibrage = "success";
                valueEquilibrage = "Équilibré (" + sommeFichiersCarthago + " = " + totalCTR + ")";
            } else {
                statusEquilibrage = "warning";
                valueEquilibrage = "Déséquilibré (" + sommeFichiersCarthago + " vs " + totalCTR + ")";
            }

            data.add(new DataRowDTO("Équilibrage Fichiers+Carthago/CTR", valueEquilibrage,
                    "Différence: " + difference, statusEquilibrage));

            // Détail de la somme
            data.add(new DataRowDTO("Fichiers générés", safeIntValue(equilibrage.getNombreFichiers()),
                    formatMontant(equilibrage.getMontantFichiers()), null));

            data.add(new DataRowDTO("Carthago traités", safeIntValue(equilibrage.getNombreCarthago()),
                    formatMontant(equilibrage.getMontantCarthago()), null));

            data.add(new DataRowDTO("Total CTR", safeIntValue(equilibrage.getNombreCTR()),
                    formatMontant(equilibrage.getMontantCTR()), null));

            // Fichiers non parvenus dans Carthago
            if (difference > 0) {
                data.add(new DataRowDTO("Fichiers non dans Carthago", safeIntValue(difference),
                        null, "warning"));
            } else if (difference < 0) {
                data.add(new DataRowDTO("Éléments Carthago en excès", safeIntValue(Math.abs(difference)),
                        null, "warning"));
            }

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    /**
     * CALCUL D'ÉQUILIBRAGE CORRIGÉ - Somme Fichiers + Carthago vs CTR
     */
    public EquilibrageResultDTO calculerEquilibrageCorrect(LocalDateTime start, LocalDateTime end) {
        try {
            // Données Fichiers (générés et validés)
            Long nombreFichiers = fichierRepository.countByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
                    start, end, true, true);
            Double montantFichiers = fichierRepository.sumMontantByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
                    start, end, true, true);

            // Données Carthago (traités = validés)
            Long nombreCarthago = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantCarthago = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");

            // Données CTR totales
            Long nombreCTR = ctrRepository.countByCreatedAtBetween(start, end);
            Double montantCTR = ctrRepository.sumMontantByCreatedAtBetween(start, end);

            // Gestion des valeurs null
            nombreFichiers = nombreFichiers != null ? nombreFichiers : 0L;
            montantFichiers = montantFichiers != null ? montantFichiers : 0.0;
            nombreCarthago = nombreCarthago != null ? nombreCarthago : 0L;
            montantCarthago = montantCarthago != null ? montantCarthago : 0.0;
            nombreCTR = nombreCTR != null ? nombreCTR : 0L;
            montantCTR = montantCTR != null ? montantCTR : 0.0;

            // Calculs d'équilibrage: (Fichiers + Carthago) = CTR
            Long totalElements = nombreFichiers + nombreCarthago;
            Double totalMontant = montantFichiers + montantCarthago;

            boolean equilibreNombre = totalElements.equals(nombreCTR);
            boolean equilibreMontant = Math.abs(totalMontant - montantCTR) < 0.01;

            Double difference = totalMontant - montantCTR;

            return new EquilibrageResultDTO(
                    nombreCarthago, montantCarthago,
                    nombreFichiers, montantFichiers,
                    nombreCTR, montantCTR,
                    equilibreNombre, equilibreMontant,
                    difference
            );

        } catch (Exception e) {
            // Retourner un équilibrage par défaut en cas d'erreur
            return new EquilibrageResultDTO(0L, 0.0, 0L, 0.0, 0L, 0.0, false, false, 0.0);
        }
    }

    // === MÉTHODES UTILITAIRES ===

    public SyntheseDTO genererSynthese(LocalDateTime start, LocalDateTime end) {
        SyntheseDTO synthese = new SyntheseDTO();

        try {
            // Statistiques Fichiers
            synthese.setTotalRemises(fichierRepository.countByCreatedAtBetween(start, end));
            synthese.setRemisesValidees(fichierRepository.countByCreatedAtBetweenAndValidationBO(start, end, true));
            synthese.setRemisesWeb(fichierRepository.countByCreatedAtBetweenAndOrigineSaisie(start, end, "WEB"));
            synthese.setRemisesEnCours(fichierRepository.countByCreatedAtBetweenAndStatutRemise(start, end, "EN_COURS"));

            // Statistiques Carthago
            synthese.setTotalCheques(carthagoRepository.countByCreatedAtBetween(start, end));
            synthese.setChequesTraites(carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE"));
            synthese.setChequesAVerifier(carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true));
            synthese.setChequesElectroniques(carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "ELECTRONIQUE"));
            synthese.setChequesManuels(carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "MANUEL"));

            // Statistiques CTR
            synthese.setTotalCTR(ctrRepository.countByCreatedAtBetween(start, end));
            synthese.setCtrEquilibres(ctrRepository.countByCreatedAtBetweenAndEquilibre(start, end, true));
            synthese.setRemisesDouble(ctrRepository.countByCreatedAtBetweenAndRemiseDouble(start, end, true));
            synthese.setRemisesNonParvenues(ctrRepository.countByCreatedAtBetweenAndRemiseNonParvenue(start, end, true));

            // Montants
            synthese.setMontantTotalRemises(fichierRepository.sumMontantByCreatedAtBetween(start, end));
            synthese.setMontantTotalCheques(carthagoRepository.sumMontantByCreatedAtBetween(start, end));
            synthese.setMontantTotalCTR(ctrRepository.sumMontantByCreatedAtBetween(start, end));

            // Calculs de taux
            synthese.calculerTaux();

        } catch (Exception e) {
            // Initialiser avec des valeurs par défaut en cas d'erreur
            synthese.setTotalRemises(0L);
            synthese.setTotalCheques(0L);
            synthese.setTotalCTR(0L);
        }

        return synthese;
    }

    public List<AnomalieDTO> detecterAnomalies(LocalDateTime start, LocalDateTime end) {
        List<AnomalieDTO> anomalies = new ArrayList<>();

        try {
            // Vérification équilibrage corrigé
            EquilibrageResultDTO equilibrage = calculerEquilibrageCorrect(start, end);
            if (!equilibrage.isEquilibreNombre()) {
                Long difference = equilibrage.getTotalCarFich() - equilibrage.getNombreCTR();
                anomalies.add(new AnomalieDTO("EQUILIBRAGE_NOMBRE",
                        "Déséquilibre détecté: " + equilibrage.getTotalCarFich() + " vs " + equilibrage.getNombreCTR() +
                                " (Différence: " + difference + ")",
                        "CRITIQUE"));
            }
            if (!equilibrage.isEquilibreMontant()) {
                anomalies.add(new AnomalieDTO("EQUILIBRAGE_MONTANT",
                        "Déséquilibre montant: " + formatMontant(equilibrage.getDifference()),
                        "CRITIQUE"));
            }

            // Vérification fichiers non parvenus dans Carthago
            Long fichiersGeneres = fichierRepository.countByCreatedAtBetweenAndGenereParEncaisse(start, end, true);
            Long fichiersReçus = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            Long fichiersNonParvenus = fichiersGeneres - fichiersReçus;

            if (fichiersNonParvenus > 0) {
                anomalies.add(new AnomalieDTO("FICHIERS_NON_PARVENUES",
                        fichiersNonParvenus + " fichiers générés non parvenus à Carthago", "ALERTE"));
            }

            // Vérification chèques à vérifier
            Long chequesAVerifier = carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true);
            if (chequesAVerifier > 10) {
                anomalies.add(new AnomalieDTO("CHEQUES_A_VERIFIER",
                        chequesAVerifier + " chèques nécessitent une vérification", "ATTENTION"));
            }

        } catch (Exception e) {
            anomalies.add(new AnomalieDTO("SYSTEM_ERROR",
                    "Erreur lors de la détection d'anomalies: " + e.getMessage(), "CRITIQUE"));
        }

        return anomalies;
    }

    public PerformanceDTO calculerPerformance(LocalDateTime start, LocalDateTime end) {
        PerformanceDTO performance = new PerformanceDTO();

        try {
            performance.setPeriodeDebut(start);
            performance.setPeriodeFin(end);

            // Métriques de volume
            performance.setVolumeRemisesTraitees(fichierRepository.countByCreatedAtBetweenAndValidationBO(start, end, true));
            performance.setVolumeChequesTraites(carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE"));
            performance.setVolumeCTRTraites(ctrRepository.countByCreatedAtBetweenAndEquilibre(start, end, true));

            // Métriques de qualité
            Long totalCheques = carthagoRepository.countByCreatedAtBetween(start, end);
            Long chequesAVerifier = carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true);

            Double tauxErreur = totalCheques > 0 ? (chequesAVerifier * 100.0) / totalCheques : 0.0;
            performance.setTauxErreurGlobal(tauxErreur);

            // Métriques métier
            performance.setMontantTotalTraite(carthagoRepository.sumMontantByCreatedAtBetween(start, end));

            Long totalRemises = fichierRepository.countByCreatedAtBetween(start, end);
            Double montantRemises = fichierRepository.sumMontantByCreatedAtBetween(start, end);
            performance.setMontantMoyenParRemise(totalRemises > 0 ? montantRemises / totalRemises : 0.0);

            Double montantCheques = carthagoRepository.sumMontantByCreatedAtBetween(start, end);
            performance.setMontantMoyenParCheque(totalCheques > 0 ? montantCheques / totalCheques : 0.0);

            // Calcul des throughputs (approximatif)
            long heuresDifference = java.time.Duration.between(start, end).toHours();
            if (heuresDifference > 0) {
                performance.setThroughputRemisesParHeure(totalRemises.doubleValue() / heuresDifference);
                performance.setThroughputChequesParHeure(totalCheques.doubleValue() / heuresDifference);
            }

            // Indicateurs par défaut
            performance.setTauxDisponibiliteSysteme(99.5);
            performance.setTempsTraitementMoyenRemise(15.0);
            performance.setTempsTraitementMoyenCheque(5.0);

        } catch (Exception e) {
            // Initialiser avec des valeurs par défaut
            performance.setTauxErreurGlobal(0.0);
            performance.setVolumeRemisesTraitees(0L);
            performance.setVolumeChequesTraites(0L);
        }

        return performance;
    }

    /**
     * Helper method to safely convert Long to int, handling null values
     */
    private int safeIntValue(Long value) {
        return value != null ? value.intValue() : 0;
    }

    /**
     * Helper method to safely convert long to int
     */
    private int safeIntValue(long value) {
        return (int) value;
    }

    private String formatMontant(Double montant) {
        if (montant == null || montant == 0.0) {
            return "0 DT";
        }
        return String.format("%.2f DT", montant);
    }
}