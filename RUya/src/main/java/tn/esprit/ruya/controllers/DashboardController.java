package tn.esprit.ruya.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.DashboardResponseDTO;
import tn.esprit.ruya.services.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(
        origins = {"http://localhost:4200", "http://127.0.0.1:4200"},
        maxAge = 3600,
        allowCredentials = "false"  // Important: set to false when using wildcard origins
)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Récupère les données complètes du dashboard
     * @return DashboardResponseDTO contenant toutes les cartes et statistiques
     */
    @GetMapping("/data")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        try {
            DashboardResponseDTO dashboardData = dashboardService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // Log l'erreur
            System.err.println("Erreur lors de la récupération des données du dashboard: " + e.getMessage());
            e.printStackTrace();

            // Retourner une réponse d'erreur
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour forcer le rafraîchissement des données
     * Utile pour le bouton de rafraîchissement manuel
     */
    @PostMapping("/refresh")
    public ResponseEntity<DashboardResponseDTO> refreshDashboardData() {
        try {
            // Même logique que getDashboardData mais avec une indication de rafraîchissement
            DashboardResponseDTO dashboardData = dashboardService.getDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement des données: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour vérifier la santé du service dashboard
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dashboard service is running");
    }
}