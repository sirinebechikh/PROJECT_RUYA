package tn.esprit.ruya.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.DashboardResponseDTO;
import tn.esprit.ruya.services.DashboardService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour le Dashboard RUYA
 * Fournit les endpoints nécessaires pour l'interface Angular
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Endpoint principal pour récupérer les données du dashboard
     * GET /api/dashboard/data
     */
    @GetMapping("/data")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        try {
            DashboardResponseDTO dashboardData = dashboardService.getDashboardDataCorrected();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // Log l'erreur (vous pouvez utiliser un logger)
            System.err.println("Erreur lors de la récupération des données dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Retourner des données par défaut ou une erreur
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour rafraîchir les données du dashboard
     * POST /api/dashboard/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<DashboardResponseDTO> refreshDashboardData() {
        try {
            // Forcer un nouveau calcul des données
            DashboardResponseDTO dashboardData = dashboardService.getDashboardDataCorrected();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement des données dashboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour récupérer les données d'une période spécifique
     * GET /api/dashboard/data/period?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     */
    @GetMapping("/data/period")
    public ResponseEntity<DashboardResponseDTO> getDashboardDataForPeriod(
            @RequestParam String start,
            @RequestParam String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);
            
            DashboardResponseDTO dashboardData = dashboardService.getDashboardDataForPeriodCorrected(startDate, endDate);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des données pour la période: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de health check pour vérifier la disponibilité du service
     * GET /api/dashboard/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            // Test simple de connectivité à la base de données
            dashboardService.getDashboardDataCorrected();
            return ResponseEntity.ok("Dashboard service is healthy");
        } catch (Exception e) {
            System.err.println("Health check failed: " + e.getMessage());
            return ResponseEntity.status(503).body("Dashboard service is unhealthy: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour obtenir des statistiques détaillées
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Obtenir les données du dashboard
            DashboardResponseDTO dashboardData = dashboardService.getDashboardDataCorrected();
            
            // Calculer des statistiques supplémentaires
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
            
            DashboardService.ResultatEquilibrageDTO equilibrage = 
                dashboardService.calculerEquilibrageGlobalCorrect(startOfDay, endOfDay);
            
            stats.put("timestamp", now.toString());
            stats.put("period", "today");
            stats.put("cardCount", dashboardData.getCardData().size());
            stats.put("globalStatsCount", dashboardData.getGlobalStats().size());
            stats.put("equilibrage", equilibrage);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des statistiques: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour obtenir l'équilibrage global
     * GET /api/dashboard/equilibrage
     */
    @GetMapping("/equilibrage")
    public ResponseEntity<DashboardService.ResultatEquilibrageDTO> getEquilibrageGlobal() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
            
            DashboardService.ResultatEquilibrageDTO equilibrage = 
                dashboardService.calculerEquilibrageGlobalCorrect(startOfDay, endOfDay);
            
            return ResponseEntity.ok(equilibrage);
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de l'équilibrage: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour obtenir l'équilibrage pour une période donnée
     * GET /api/dashboard/equilibrage/period?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     */
    @GetMapping("/equilibrage/period")
    public ResponseEntity<DashboardService.ResultatEquilibrageDTO> getEquilibrageForPeriod(
            @RequestParam String start,
            @RequestParam String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startDate = LocalDateTime.parse(start, formatter);
            LocalDateTime endDate = LocalDateTime.parse(end, formatter);
            
            DashboardService.ResultatEquilibrageDTO equilibrage = 
                dashboardService.calculerEquilibrageGlobalCorrect(startDate, endDate);
            
            return ResponseEntity.ok(equilibrage);
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de l'équilibrage pour la période: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint de test simple pour vérifier la connectivité
     * GET /api/dashboard/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "Dashboard RUYA");
        return ResponseEntity.ok(response);
    }
}