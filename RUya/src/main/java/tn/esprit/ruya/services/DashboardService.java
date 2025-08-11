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

        // Construction des cartes avec calculs dynamiques corrigés
        cardData.add(buildClientExterneCardDynamic(startOfDay, endOfDay));
        cardData.add(buildSessionDuJourCardDynamic(startOfDay, endOfDay));
        cardData.add(buildFGenererCardDynamic(startOfDay, endOfDay));
        cardData.add(buildClientExterneWebCardDynamic(startOfDay, endOfDay));
        cardData.add(buildFichiersCarthagoDynamic(startOfDay, endOfDay));
        cardData.add(buildValidationBOCardDynamic(startOfDay, endOfDay));
        cardData.add(buildCarthagoAvantCTRCardDynamic(startOfDay, endOfDay));
        cardData.add(buildRemisesCTRCardDynamic(startOfDay, endOfDay));
        cardData.add(buildActionsControlesCardDynamic(startOfDay, endOfDay));

        response.setCardData(cardData);
        response.setGlobalStats(buildGlobalStatsDynamic(startOfDay, endOfDay));

        return response;
    }

    private CardDataDTO buildClientExterneCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Client Externe");
        card.setIcon("fas fa-user-circle");
        card.setType("primary");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Statut - vérification des clients actifs
            Long clientsActifs = fichierRepository.countByCreatedAtBetweenAndClientExterneIdIsNotNull(start, end);
            String statutValue = clientsActifs > 0 ? "Actif" : "Inactif";
            String statutStatus = clientsActifs > 0 ? "success" : "warning";
            data.add(new DataRowDTO("Statut", statutValue, null, statutStatus));

            // En cours de saisie - utilisation du statut remise
            Long enCoursSaisie = fichierRepository.countByCreatedAtBetweenAndStatutRemise(start, end, "EN_COURS");
            Double montantEnCours = fichierRepository.sumMontantByCreatedAtBetweenAndStatutRemise(start, end, "EN_COURS");
            data.add(new DataRowDTO("En cours de saisie", enCoursSaisie.intValue(),
                    formatMontant(montantEnCours), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildSessionDuJourCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Session du Jour");
        card.setIcon("fas fa-calendar");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Chèques traités
            Long chequesTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Double montantTraites = carthagoRepository.sumMontantByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            data.add(new DataRowDTO("Chèques traités", chequesTraites.intValue(),
                    formatMontant(montantTraites), null));

            // Chèques électroniques
            Long chequesElec = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "ELECTRONIQUE");
            Double montantElec = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "ELECTRONIQUE");
            data.add(new DataRowDTO("Chèques électroniques", chequesElec.intValue(),
                    formatMontant(montantElec), null));

            // Chèques manuels
            Long chequesManuels = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "MANUEL");
            Double montantManuels = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "MANUEL");
            data.add(new DataRowDTO("Chèques manuels", chequesManuels.intValue(),
                    formatMontant(montantManuels), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildFGenererCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("F_GENERER par Encaisse");
        card.setIcon("fas fa-cogs");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises générées par encaisse
            Long remisesParEncaisse = fichierRepository.countByCreatedAtBetweenAndGenereParEncaisse(start, end, true);
            Double montantParEncaisse = fichierRepository.sumMontantByCreatedAtBetweenAndGenereParEncaisse(start, end, true);
            data.add(new DataRowDTO("Remises par encaisse", remisesParEncaisse.intValue(),
                    formatMontant(montantParEncaisse), null));

            // Total des remises
            Long totalRemises = fichierRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            Double totalMontant = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            data.add(new DataRowDTO("Total des remises", totalRemises.intValue(),
                    formatMontant(totalMontant), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildClientExterneWebCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Client Externe Web");
        card.setIcon("fas fa-globe");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises créées via web
            Long remisesWeb = fichierRepository.countByCreatedAtBetweenAndOrigineSaisie(start, end, "WEB");
            Double montantWeb = fichierRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
            data.add(new DataRowDTO("Remises créées", remisesWeb.intValue(),
                    formatMontant(montantWeb), null));

            // Chèques web
            Long chequesWeb = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
            Double montantChequesWeb = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
            data.add(new DataRowDTO("Chèques web", chequesWeb.intValue(),
                    formatMontant(montantChequesWeb), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildFichiersCarthagoDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Fichiers Carthago");
        card.setIcon("fas fa-server");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Consommés par Carthago
            Long consommes = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            Double montantConsommes = carthagoRepository.sumMontantByCreatedAtBetweenAndSens(start, end, "ENTRANT");
            data.add(new DataRowDTO("Consommés par Carthago", consommes.intValue(),
                    formatMontant(montantConsommes), null));

            // Générés vers CTR
            Long generesCTR = ctrRepository.countByCreatedAtBetweenAndTypeOperation(start, end, "ENVOI");
            Double montantGeneres = ctrRepository.sumMontantByCreatedAtBetweenAndStatutCtr(start, end, "GENERE");
            data.add(new DataRowDTO("Générés vers CTR", generesCTR.intValue(),
                    formatMontant(montantGeneres), null));

            // Reçus par CTR
            Long recusCTR = ctrRepository.countByCreatedAtBetweenAndTypeOperation(start, end, "RECEPTION");
            Double montantRecus = ctrRepository.sumMontantByCreatedAtBetweenAndStatutCtr(start, end, "RECU");
            data.add(new DataRowDTO("Reçus par CTR", recusCTR.intValue(),
                    formatMontant(montantRecus), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildValidationBOCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Validation BO Dinars");
        card.setIcon("fas fa-check-double");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises validées BO
            Long remisesBO = fichierRepository.countByCreatedAtBetweenAndValidationBO(start, end, true);
            Double montantBO = fichierRepository.sumMontantByCreatedAtBetweenAndValidationBO(start, end, true);
            data.add(new DataRowDTO("Remises validées BO", remisesBO.intValue(),
                    formatMontant(montantBO), null));

            // Chèques BO Dinars
            Long chequesBODinars = carthagoRepository.countByCreatedAtBetweenAndValideBodinars(start, end, true);
            Double montantBODinars = carthagoRepository.sumMontantByCreatedAtBetweenAndValideBodinars(start, end, true);
            data.add(new DataRowDTO("Chèques BO Dinars", chequesBODinars.intValue(),
                    formatMontant(montantBODinars), null));

            // Chèques Web BO
            Long chequesWebBO = carthagoRepository.countByCreatedAtBetweenAndChequeWebBo(start, end, true);
            Double montantWebBO = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
            data.add(new DataRowDTO("Chèques Web BO", chequesWebBO.intValue(),
                    formatMontant(montantWebBO), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildCarthagoAvantCTRCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Carthago avant CTR");
        card.setIcon("fas fa-exchange-alt");
        card.setType("warning");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises avant CTR
            Long remisesAvantCTR = carthagoRepository.countByCreatedAtBetweenAndAvantCTR(start, end, true);
            Double montantAvantCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndAvantCTR(start, end, true);
            data.add(new DataRowDTO("Nombre de remises", remisesAvantCTR.intValue(),
                    formatMontant(montantAvantCTR), null));

            // Images STATUT 3
            Long imagesStatut3 = carthagoRepository.countByCreatedAtBetweenAndStatutImage(start, end, 3);
            String statutImages = imagesStatut3 > 0 ? "warning" : "success";
            data.add(new DataRowDTO("Images STATUT 3", imagesStatut3.intValue(), null, statutImages));

            // Chèques fichier
            Long chequesFichier = carthagoRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "FICHIER");
            Double montantFichier = carthagoRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "FICHIER");
            data.add(new DataRowDTO("Chèques fichier", chequesFichier.intValue(),
                    formatMontant(montantFichier), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildRemisesCTRCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Remises CTR");
        card.setIcon("fas fa-copy");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises en double
            Long remisesDouble = ctrRepository.countByCreatedAtBetweenAndRemiseDouble(start, end, true);
            Double montantDouble = ctrRepository.sumMontantByCreatedAtBetweenAndStatutCtr(start, end, "DOUBLE");
            data.add(new DataRowDTO("Remises en double", remisesDouble.intValue(),
                    formatMontant(montantDouble), null));

            // Chèques électroniques CTR
            Long chequesElecCTR = carthagoRepository.countByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(start, end, "ELECTRONIQUE", true);
            Double montantElecCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(start, end, "ELECTRONIQUE", true);
            data.add(new DataRowDTO("Chèques électroniques", chequesElecCTR.intValue(),
                    formatMontant(montantElecCTR), null));

            // Chèques fichier ENV
            Long chequesFichierENV = carthagoRepository.countByCreatedAtBetweenAndFichierEnv(start, end, true);
            Double montantENV = carthagoRepository.sumMontantByCreatedAtBetweenAndFichierEnv(start, end, true);
            data.add(new DataRowDTO("Chèques fichier ENV", chequesFichierENV.intValue(),
                    formatMontant(montantENV), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private CardDataDTO buildActionsControlesCardDynamic(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Actions et Contrôles");
        card.setIcon("fas fa-tools");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        try {
            // Remises non parvenues
            Long remisesNonParvenues = ctrRepository.countByCreatedAtBetweenAndRemiseNonParvenue(start, end, true);
            String statusRemises = remisesNonParvenues > 0 ? "warning" : "success";
            String valueRemises = remisesNonParvenues > 0 ? "En attente" : "OK";
            data.add(new DataRowDTO("Remises non parvenues", valueRemises, null, statusRemises));

            // CARTHAGO après CTR - calcul d'équilibrage
            EquilibrageResultDTO equilibrage = calculerEquilibrage(start, end);
            String statusCarthago = equilibrage.isEquilibreTotal() ? "success" : "danger";
            String valueCarthago = equilibrage.isEquilibreTotal() ? "Équilibré" : "Faux";

            data.add(new DataRowDTO("CARTHAGO après CTR", valueCarthago,
                    formatMontant(equilibrage.getTotalMontantCarFich()), statusCarthago));

            // Chèques à vérifier
            Long chequesAVerifier = carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true);
            Double montantAVerifier = carthagoRepository.sumMontantByCreatedAtBetweenAndAVerifier(start, end, true);
            data.add(new DataRowDTO("Chèques à vérifier", chequesAVerifier.intValue(),
                    formatMontant(montantAVerifier), null));

        } catch (Exception e) {
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }

        card.setData(data);
        return card;
    }

    private List<StatCardDTO> buildGlobalStatsDynamic(LocalDateTime start, LocalDateTime end) {
        List<StatCardDTO> stats = new ArrayList<>();

        try {
            // Total Remises
            Long totalRemises = fichierRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            Double totalMontantRemises = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
            stats.add(new StatCardDTO(totalRemises.toString(), "Total Remises",
                    formatMontant(totalMontantRemises)));

            // Total Chèques
            Long totalCheques = carthagoRepository.countByCreatedAtBetween(start, end);
            Double totalMontantCheques = carthagoRepository.sumMontantByCreatedAtBetween(start, end);
            stats.add(new StatCardDTO(totalCheques.toString(), "Total Chèques",
                    formatMontant(totalMontantCheques)));

            // Taux de Réussite
            Long chequesTraites = carthagoRepository.countByCreatedAtBetweenAndStatutCheque(start, end, "TRAITE");
            Long chequesControles = carthagoRepository.countByCreatedAtBetweenAndControleEffectue(start, end, true);
            Long chequesEquilibres = ctrRepository.countByCreatedAtBetweenAndEquilibre(start, end, true);

            Double tauxTraitement = totalCheques > 0 ? (chequesTraites * 100.0) / totalCheques : 0.0;
            Double tauxControle = totalCheques > 0 ? (chequesControles * 100.0) / totalCheques : 0.0;

            Long totalCTR = ctrRepository.countByCreatedAtBetween(start, end);
            Double tauxEquilibrage = totalCTR > 0 ? (chequesEquilibres * 100.0) / totalCTR : 0.0;

            Double tauxGlobal = (tauxTraitement + tauxControle + tauxEquilibrage) / 3.0;

            String statusTaux = tauxGlobal >= 95 ? "Excellent" : tauxGlobal >= 80 ? "Bon" : "Moyen";
            stats.add(new StatCardDTO(String.format("%.1f%%", tauxGlobal), "Taux de Réussite", statusTaux));

            // Dernière MAJ
            String derniereMaj = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            stats.add(new StatCardDTO(derniereMaj, "Dernière MAJ", null));

        } catch (Exception e) {
            stats.add(new StatCardDTO("Erreur", "Statistiques indisponibles", null));
        }

        return stats;
    }

    // === MÉTHODES UTILITAIRES ===

    public EquilibrageResultDTO calculerEquilibrage(LocalDateTime start, LocalDateTime end) {
        try {
            // Données Carthago
            Long nombreCarthago = carthagoRepository.countByCreatedAtBetweenAndApresCTR(start, end, true);
            Double montantCarthago = carthagoRepository.sumMontantByCreatedAtBetweenAndApresCTR(start, end, true);

            // Données Fichiers
            Long nombreFichiers = fichierRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "APRES_CTR");
            Double montantFichiers = fichierRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "APRES_CTR");

            // Données CTR
            Long nombreCTR = ctrRepository.countByCreatedAtBetween(start, end);
            Double montantCTR = ctrRepository.sumMontantByCreatedAtBetween(start, end);

            // Gestion des valeurs null
            nombreCarthago = nombreCarthago != null ? nombreCarthago : 0L;
            montantCarthago = montantCarthago != null ? montantCarthago : 0.0;
            nombreFichiers = nombreFichiers != null ? nombreFichiers : 0L;
            montantFichiers = montantFichiers != null ? montantFichiers : 0.0;
            nombreCTR = nombreCTR != null ? nombreCTR : 0L;
            montantCTR = montantCTR != null ? montantCTR : 0.0;

            // Calculs d'équilibrage
            Long totalCarFich = nombreCarthago + nombreFichiers;
            Double totalMontantCarFich = montantCarthago + montantFichiers;

            boolean equilibreNombre = totalCarFich.equals(nombreCTR);
            boolean equilibreMontant = Math.abs(totalMontantCarFich - montantCTR) < 0.01;

            return new EquilibrageResultDTO(
                    nombreCarthago, montantCarthago,
                    nombreFichiers, montantFichiers,
                    nombreCTR, montantCTR,
                    equilibreNombre, equilibreMontant,
                    totalMontantCarFich - montantCTR
            );
        } catch (Exception e) {
            // Retourner un équilibrage par défaut en cas d'erreur
            return new EquilibrageResultDTO(0L, 0.0, 0L, 0.0, 0L, 0.0, false, false, 0.0);
        }
    }

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
            // Vérification équilibrage
            EquilibrageResultDTO equilibrage = calculerEquilibrage(start, end);
            if (!equilibrage.isEquilibreNombre()) {
                anomalies.add(new AnomalieDTO("EQUILIBRAGE_NOMBRE",
                        "Déséquilibre détecté dans les nombres", "CRITIQUE"));
            }
            if (!equilibrage.isEquilibreMontant()) {
                anomalies.add(new AnomalieDTO("EQUILIBRAGE_MONTANT",
                        "Déséquilibre détecté dans les montants", "CRITIQUE"));
            }

            // Vérification remises non parvenues
            Long remisesNonParvenues = ctrRepository.countByCreatedAtBetweenAndRemiseNonParvenue(start, end, true);
            if (remisesNonParvenues != null && remisesNonParvenues > 0) {
                anomalies.add(new AnomalieDTO("REMISES_NON_PARVENUES",
                        remisesNonParvenues + " remises non parvenues", "ALERTE"));
            }

            // Vérification images statut 3
            Long imagesStatut3 = carthagoRepository.countByCreatedAtBetweenAndStatutImage(start, end, 3);
            if (imagesStatut3 != null && imagesStatut3 > 0) {
                anomalies.add(new AnomalieDTO("IMAGES_STATUT3",
                        imagesStatut3 + " images en statut 3", "ATTENTION"));
            }

            // Vérification chèques à vérifier
            Long chequesAVerifier = carthagoRepository.countByCreatedAtBetweenAndAVerifier(start, end, true);
            if (chequesAVerifier != null && chequesAVerifier > 10) {
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
            performance.setTauxDisponibiliteSysteme(99.5); // À calculer selon la logique métier
            performance.setTempsTraitementMoyenRemise(15.0); // En minutes, à calculer
            performance.setTempsTraitementMoyenCheque(5.0); // En minutes, à calculer

        } catch (Exception e) {
            // Initialiser avec des valeurs par défaut
            performance.setTauxErreurGlobal(0.0);
            performance.setVolumeRemisesTraitees(0L);
            performance.setVolumeChequesTraites(0L);
        }

        return performance;
    }

    private String formatMontant(Double montant) {
        if (montant == null || montant == 0.0) {
            return "0 DT";
        }
        return String.format("%.2f DT", montant);
    }
}