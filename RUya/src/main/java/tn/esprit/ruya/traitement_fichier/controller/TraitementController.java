package tn.esprit.ruya.traitement_fichier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.ruya.models.traitement_fichiers;
import tn.esprit.ruya.traitement_fichier.service.TraitementServ;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/traitements")
public class TraitementController {

    @Autowired
    private TraitementServ traitementServ;

    @GetMapping
    public ResponseEntity<List<traitement_fichiers>> getAllTraitements() {
        return ResponseEntity.ok(traitementServ.getAllTraitements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<traitement_fichiers> getTraitementById(@PathVariable Long id) {
        Optional<traitement_fichiers> traitement = traitementServ.getTraitementById(id);
        return traitement.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<traitement_fichiers> createTraitement(@RequestBody traitement_fichiers traitement) {
        traitement_fichiers created = traitementServ.createTraitement(traitement);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<traitement_fichiers> updateTraitement(
            @PathVariable Long id,
            @RequestBody traitement_fichiers updatedTraitement) {
        traitement_fichiers updated = traitementServ.updateTraitement(id, updatedTraitement);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraitement(@PathVariable Long id) {
        traitementServ.deleteTraitement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        traitementServ.processFile(file);  // ✅ appel correct de l’instance
        return ResponseEntity.ok("Fichier traité avec succès");
    }
}
