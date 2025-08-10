package tn.esprit.ruya.traitement_fichier.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.traitement_fichiers;
import tn.esprit.ruya.traitement_fichier.repository.ITraaitementrepo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TraitementServ implements ITraitementServ {

    @Autowired
    private ITraaitementrepo traitementRepo;

    @Override
    public List<traitement_fichiers> getAllTraitements() {
        return traitementRepo.findAll();
    }

    @Override
    public Optional<traitement_fichiers> getTraitementById(Long id) {
        return traitementRepo.findById(id);
    }

    @Override
    public traitement_fichiers createTraitement(traitement_fichiers traitement) {
        return traitementRepo.save(traitement);
    }

    @Override
    public traitement_fichiers updateTraitement(Long id, traitement_fichiers updatedTraitement) {
        return traitementRepo.findById(id).map(traitement -> {
            traitement.setFichier(updatedTraitement.getFichier());
            traitement.setPathReception(updatedTraitement.getPathReception());
            traitement.setPathEnvoie(updatedTraitement.getPathEnvoie());
            traitement.setDateTraitement(updatedTraitement.getDateTraitement());
            traitement.setNombre(updatedTraitement.getNombre());
            traitement.setMontant(updatedTraitement.getMontant());
            traitement.setStatut(updatedTraitement.getStatut());
            return traitementRepo.save(traitement);
        }).orElse(null);
    }

    @Override
    public void deleteTraitement(Long id) {
        traitementRepo.deleteById(id);
    }

    @Override
    public void processFile(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                traitement_fichiers tf = parseLine(line);
                traitementRepo.save(tf);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }

    private traitement_fichiers parseLine(String line) {
        traitement_fichiers tf = new traitement_fichiers();

        try {
            String idStr = line.substring(0, 5).trim();
            String nomFichier = line.substring(5, 21).trim();
            String codeFichier = line.substring(21, 29).trim();
            String typeFichier = line.substring(29, 36).trim();
            String dateStr = line.substring(36, 44).trim();
            String montantStr = line.substring(44, 55).trim();
            String codeStatut = line.substring(55, 57).trim();

            tf.setNombre(Integer.parseInt(idStr));
            tf.setPathReception(nomFichier);
            tf.setPathEnvoie(codeFichier);
            tf.setStatut(codeStatut);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            tf.setDateTraitement(LocalDate.parse(dateStr, formatter));

            tf.setMontant(new BigDecimal(montantStr));

            // Fichier fictif (à améliorer selon ton contexte)
            Fichier fichier = new Fichier();
            fichier.setNomFichier(nomFichier);
            fichier.setNatureFichier(typeFichier);
            fichier.setCodeValeur(codeFichier);

            tf.setFichier(fichier); // si tu as un repo Fichier, cherche-le ici

        } catch (Exception e) {
            throw new RuntimeException("Erreur de parsing ligne : " + line);
        }

        return tf;
    }
}
