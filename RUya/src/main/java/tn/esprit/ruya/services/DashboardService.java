package tn.esprit.ruya.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.CardDataDTO;
import tn.esprit.ruya.models.DataRowDTO;
import tn.esprit.ruya.models.DashboardResponseDTO;
import tn.esprit.ruya.models.StatCardDTO;
import tn.esprit.ruya.repositories.CarthagoRepository;
import tn.esprit.ruya.repositories.FichierRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private CarthagoRepository carthagoRepository;

    @Autowired
    private FichierRepository fichierRepository;

    public DashboardResponseDTO getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        DashboardResponseDTO response = new DashboardResponseDTO();

        // Construction des cartes du dashboard
        List<CardDataDTO> cardData = new ArrayList<>();

        // 1. Client Externe
        cardData.add(buildClientExterneCard(startOfDay, endOfDay));

        // 2. Session du Jour
        cardData.add(buildSessionDuJourCard(startOfDay, endOfDay));

        // 3. F_GENERER par Encaisse
        cardData.add(buildFGenererCard(startOfDay, endOfDay));

        // 4. Client Externe Web
        cardData.add(buildClientExterneWebCard(startOfDay, endOfDay));

        // 5. Fichiers Carthago
        cardData.add(buildFichiersCarthago(startOfDay, endOfDay));

        // 6. Validation BO Dinars
        cardData.add(buildValidationBOCard(startOfDay, endOfDay));

        // 7. Carthago avant CTR
        cardData.add(buildCarthagoAvantCTRCard(startOfDay, endOfDay));

        // 8. Remises CTR
        cardData.add(buildRemisesCTRCard(startOfDay, endOfDay));

        // 9. Actions et Contrôles
        cardData.add(buildActionsControlesCard(startOfDay, endOfDay));

        response.setCardData(cardData);
        response.setGlobalStats(buildGlobalStats(startOfDay, endOfDay));

        return response;
    }

    private CardDataDTO buildClientExterneCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Client Externe");
        card.setIcon("fas fa-user-circle");
        card.setType("primary");

        List<DataRowDTO> data = new ArrayList<>();

        // Statut - toujours actif pour cette démo
        data.add(new DataRowDTO("Statut", "Actif", null, "success"));

        // En cours de saisie - fichiers créés aujourd'hui mais pas encore traités
        Integer enCoursSaisie = fichierRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "EN_SAISIE");
        Double montantEnCours = fichierRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "EN_SAISIE");

        data.add(new DataRowDTO("En cours de saisie", enCoursSaisie,
                formatMontant(montantEnCours), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildSessionDuJourCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Session du Jour");
        card.setIcon("fas fa-calendar");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Chèques traités
        Integer chequesTraites = carthagoRepository.countByCreatedAtBetween(start, end);
        Double montantTraites = carthagoRepository.sumMontantByCreatedAtBetween(start, end);
        data.add(new DataRowDTO("Chèques traités", chequesTraites,
                formatMontant(montantTraites), null));

        // Chèques électroniques
        Integer chequesElec = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "ELECTRONIQUE");
        Double montantElec = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "ELECTRONIQUE");
        data.add(new DataRowDTO("Chèques électroniques", chequesElec,
                formatMontant(montantElec), null));

        // Chèques manuels
        Integer chequesManuels = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "MANUEL");
        Double montantManuels = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "MANUEL");
        data.add(new DataRowDTO("Chèques manuels", chequesManuels,
                formatMontant(montantManuels), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildFGenererCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("F_GENERER par Encaisse");
        card.setIcon("fas fa-cogs");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Nombre de remises générées
        Integer nbRemises = fichierRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
        Double montantRemises = fichierRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "REMISE");
        data.add(new DataRowDTO("Nombre de remises", nbRemises,
                formatMontant(montantRemises), null));

        // Total des remises
        Integer totalRemises = fichierRepository.countByCreatedAtBetween(start, end);
        Double totalMontant = fichierRepository.sumMontantByCreatedAtBetween(start, end);
        data.add(new DataRowDTO("Total des remises", totalRemises,
                formatMontant(totalMontant), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildClientExterneWebCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Client Externe Web");
        card.setIcon("fas fa-globe");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();

        // Remises créées via web
        Integer remisesWeb = fichierRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
        Double montantWeb = fichierRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
        data.add(new DataRowDTO("Remises créées", remisesWeb,
                formatMontant(montantWeb), null));

        // Chèques web
        Integer chequesWeb = carthagoRepository.countByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
        Double montantChequesWeb = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichier(start, end, "WEB");
        data.add(new DataRowDTO("Chèques web", chequesWeb,
                formatMontant(montantChequesWeb), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildFichiersCarthago(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Fichiers Carthago");
        card.setIcon("fas fa-server");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Consommés par Carthago
        Integer consommes = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "ENTRANT");
        Double montantConsommes = carthagoRepository.sumMontantByCreatedAtBetweenAndSens(start, end, "ENTRANT");
        data.add(new DataRowDTO("Consommés par Carthago", consommes,
                formatMontant(montantConsommes), null));

        // Générés vers CTR
        Integer generesCTR = carthagoRepository.countByCreatedAtBetweenAndSens(start, end, "SORTANT");
        Double montantGeneres = carthagoRepository.sumMontantByCreatedAtBetweenAndSens(start, end, "SORTANT");
        data.add(new DataRowDTO("Générés vers CTR", generesCTR,
                formatMontant(montantGeneres), null));

        // Reçus par CTR
        Integer recusCTR = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "CTR_RECU");
        Double montantRecus = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "CTR_RECU");
        data.add(new DataRowDTO("Reçus par CTR", recusCTR,
                formatMontant(montantRecus), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildValidationBOCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Validation BO Dinars");
        card.setIcon("fas fa-check-double");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Remises validées BO
        Integer remisesBO = fichierRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "BO_VALIDE");
        Double montantBO = fichierRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "BO_VALIDE");
        data.add(new DataRowDTO("Remises validées BO", remisesBO,
                formatMontant(montantBO), null));

        // Chèques BO Dinars
        Integer chequesBODinars = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "BO_DINARS");
        Double montantBODinars = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "BO_DINARS");
        data.add(new DataRowDTO("Chèques BO Dinars", chequesBODinars,
                formatMontant(montantBODinars), null));

        // Chèques Web BO
        Integer chequesWebBO = carthagoRepository.countByCreatedAtBetweenAndTypeFichierAndCodeValeur(start, end, "WEB", "BO_VALIDE");
        Double montantWebBO = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichierAndCodeValeur(start, end, "WEB", "BO_VALIDE");
        data.add(new DataRowDTO("Chèques Web BO", chequesWebBO,
                formatMontant(montantWebBO), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildCarthagoAvantCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Carthago avant CTR");
        card.setIcon("fas fa-exchange-alt");
        card.setType("warning");

        List<DataRowDTO> data = new ArrayList<>();

        // Nombre de remises avant CTR
        Integer remisesAvantCTR = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "AVANT_CTR");
        Double montantAvantCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "AVANT_CTR");
        data.add(new DataRowDTO("Nombre de remises", remisesAvantCTR,
                formatMontant(montantAvantCTR), null));

        // Images STATUT 3
        Integer imagesStatut3 = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "STATUT_3");
        data.add(new DataRowDTO("Images STATUT 3", imagesStatut3, null, "warning"));

        // Chèques fichier
        Integer chequesFichier = carthagoRepository.countByCreatedAtBetweenAndNatureFichier(start, end, "FICHIER");
        Double montantFichier = carthagoRepository.sumMontantByCreatedAtBetweenAndNatureFichier(start, end, "FICHIER");
        data.add(new DataRowDTO("Chèques fichier", chequesFichier,
                formatMontant(montantFichier), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildRemisesCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Remises CTR");
        card.setIcon("fas fa-copy");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Remises en double
        Integer remisesDouble = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "DOUBLE");
        Double montantDouble = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "DOUBLE");
        data.add(new DataRowDTO("Remises en double", remisesDouble,
                formatMontant(montantDouble), null));

        // Chèques électroniques CTR
        Integer chequesElecCTR = carthagoRepository.countByCreatedAtBetweenAndTypeFichierAndCodeValeur(start, end, "ELECTRONIQUE", "CTR");
        Double montantElecCTR = carthagoRepository.sumMontantByCreatedAtBetweenAndTypeFichierAndCodeValeur(start, end, "ELECTRONIQUE", "CTR");
        data.add(new DataRowDTO("Chèques électroniques", chequesElecCTR,
                formatMontant(montantElecCTR), null));

        // Chèques fichier ENV
        Integer chequesFichierENV = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "ENV");
        Double montantENV = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "ENV");
        data.add(new DataRowDTO("Chèques fichier ENV", chequesFichierENV,
                formatMontant(montantENV), null));

        card.setData(data);
        return card;
    }

    private CardDataDTO buildActionsControlesCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Actions et Contrôles");
        card.setIcon("fas fa-tools");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();

        // Remises non parvenues
        Integer remisesNonParvenues = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "NON_PARVENU");
        String statusRemises = remisesNonParvenues > 0 ? "warning" : "success";
        String valueRemises = remisesNonParvenues > 0 ? "En attente" : "OK";
        data.add(new DataRowDTO("Remises non parvenues", valueRemises, null, statusRemises));

        // CARTHAGO après CTR
        Integer carthagoApresCTR = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "APRES_CTR");
        String statusCarthago = carthagoApresCTR > 0 ? "success" : "warning";
        data.add(new DataRowDTO("CARTHAGO après CTR", "OK", null, statusCarthago));

        // Chèques à vérifier
        Integer chequesAVerifier = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "A_VERIFIER");
        Double montantAVerifier = carthagoRepository.sumMontantByCreatedAtBetweenAndCodeValeur(start, end, "A_VERIFIER");
        data.add(new DataRowDTO("Chèques à vérifier", chequesAVerifier,
                formatMontant(montantAVerifier), null));

        card.setData(data);
        return card;
    }

    private List<StatCardDTO> buildGlobalStats(LocalDateTime start, LocalDateTime end) {
        List<StatCardDTO> stats = new ArrayList<>();

        // Total Remises
        Integer totalRemises = fichierRepository.countByCreatedAtBetween(start, end);
        Double totalMontantRemises = fichierRepository.sumMontantByCreatedAtBetween(start, end);
        stats.add(new StatCardDTO(totalRemises.toString(), "Total Remises",
                formatMontant(totalMontantRemises)));

        // Total Chèques
        Integer totalCheques = carthagoRepository.countByCreatedAtBetween(start, end);
        Double totalMontantCheques = carthagoRepository.sumMontantByCreatedAtBetween(start, end);
        stats.add(new StatCardDTO(totalCheques.toString(), "Total Chèques",
                formatMontant(totalMontantCheques)));

        // Taux de Réussite
        Integer chequesTraites = carthagoRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "TRAITE");
        Double tauxReussite = totalCheques > 0 ? (chequesTraites * 100.0) / totalCheques : 0.0;
        String statusTaux = tauxReussite >= 95 ? "Excellent" : tauxReussite >= 80 ? "Bon" : "Moyen";
        stats.add(new StatCardDTO(String.format("%.1f%%", tauxReussite), "Taux de Réussite", statusTaux));

        // Dernière MAJ
        String derniereMaj = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        stats.add(new StatCardDTO(derniereMaj, "Dernière MAJ", null));

        return stats;
    }

    private String formatMontant(Double montant) {
        if (montant == null || montant == 0.0) {
            return "0 DT";
        }
        return String.format("%.0f DT", montant);
    }
}