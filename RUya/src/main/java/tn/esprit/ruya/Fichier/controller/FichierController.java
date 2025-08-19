package tn.esprit.ruya.Fichier.controller;

import lombok.AllArgsConstructor;
import tn.esprit.ruya.Fichier.service.FichierServ;
import tn.esprit.ruya.models.Dto;
import tn.esprit.ruya.models.Fichier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/fichiers")
@CrossOrigin(origins = "*") // Permettre les requêtes cross-origin
public class FichierController {

    private final FichierServ fichierServ;

    // ✅ Get all fichiers
    @GetMapping
    public ResponseEntity<List<Fichier>> getAllFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Get fichier by ID
    @GetMapping("/{id}")
    public ResponseEntity<Fichier> getFichierById(@PathVariable Long id) {
        try {
            Optional<Fichier> fichier = fichierServ.getFichierById(id);
            return fichier.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du fichier " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Get all fichiers by user ID
    @GetMapping("/getallbyuser/{id}")
    public ResponseEntity<List<Fichier>> getAllFichierByUser(@PathVariable Long id) {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiersByUser(id);
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers pour l'utilisateur " + id + " : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Create new fichier
    @PostMapping
    public ResponseEntity<?> createFichier(@RequestBody Fichier fichier) {
        try {
            System.out.println("📝 DEBUG - Requête POST reçue pour créer fichier: " + fichier);

            // Validation des données requises
            if (fichier.getNomFichier() == null || fichier.getNomFichier().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le nom du fichier est requis.");
            }

            if (fichier.getUser() == null || fichier.getUser().getId() == null) {
                return ResponseEntity.badRequest().body("L'utilisateur est requis pour créer un fichier.");
            }

            Fichier created = fichierServ.createFichier(fichier);
            System.out.println("📝 DEBUG - Fichier créé avec succès: " + created.getNomFichier());
            return ResponseEntity.ok(created);

        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la création du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la création du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de l'ajout du fichier");
        }
    }

    // ✅ Update fichier by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFichier(@PathVariable Long id, @RequestBody Fichier updatedFichier) {
        try {
            Fichier fichier = fichierServ.updateFichier(id, updatedFichier);
            return ResponseEntity.ok(fichier);
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la mise à jour du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la mise à jour du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la modification du fichier");
        }
    }

    // ✅ Delete fichier by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFichier(@PathVariable Long id) {
        try {
            fichierServ.deleteFichier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression du fichier");
        }
    }

    // ✅ Get DTO avec statistiques générales
    @GetMapping("/dto")
    public ResponseEntity<Dto> getAllFichiersDto() {
        try {
            Dto dto = fichierServ.getAllFichiersDto();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du DTO: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques par statut
    @GetMapping("/stats/status")
    public ResponseEntity<Map<String, Object>> getStatsByStatus() {
        try {
            Map<String, Object> stats = fichierServ.getStatsByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques par statut: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques mensuelles
    @GetMapping("/stats/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats() {
        try {
            Map<String, Object> stats = fichierServ.getMonthlyStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques mensuelles: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers avec filtres
    @GetMapping("/filter")
    public ResponseEntity<List<Fichier>> getFichiersWithFilters(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            List<Fichier> fichiers = fichierServ.getFichiersWithFilters(
                    date, statut, type, search, page, size, sortBy, sortDir);
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du filtrage des fichiers: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get montant total par type
    @GetMapping("/stats/amounts")
    public ResponseEntity<Map<String, Double>> getAmountsByType() {
        try {
            Map<String, Double> amounts = fichierServ.getAmountsByType();
            return ResponseEntity.ok(amounts);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des montants par type: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers en attente
    @GetMapping("/pending")
    public ResponseEntity<List<Fichier>> getPendingFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getPendingFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers en attente: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers récents (7 derniers jours)
    @GetMapping("/recent")
    public ResponseEntity<List<Fichier>> getRecentFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getRecentFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers récents: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques par utilisateur
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getStatsByUser(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = fichierServ.getStatsByUser(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques utilisateur: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get alertes (fichiers rejetés récents)
    @GetMapping("/alerts")
    public ResponseEntity<List<Fichier>> getAlerts() {
        try {
            List<Fichier> alertes = fichierServ.getAlerts();
            return ResponseEntity.ok(alertes);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des alertes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get count total des fichiers
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiers();
            return ResponseEntity.ok((long) fichiers.size());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du comptage des fichiers: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Batch operations - Supprimer plusieurs fichiers
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteFichiers(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                fichierServ.deleteFichier(id);
            }
            return ResponseEntity.ok("Fichiers supprimés avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression en lot: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression en lot");
        }
    }

    // 🆕 Update status d'un fichier
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateFichierStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Optional<Fichier> fichierOpt = fichierServ.getFichierById(id);
            if (fichierOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Fichier fichier = fichierOpt.get();
            fichier.setCodeValeur(status);

            Fichier updated = fichierServ.updateFichier(id, fichier);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la mise à jour du statut");
        }
    }
}