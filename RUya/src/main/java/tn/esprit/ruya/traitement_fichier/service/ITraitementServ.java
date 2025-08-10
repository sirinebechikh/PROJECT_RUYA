package tn.esprit.ruya.traitement_fichier.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.ruya.models.traitement_fichiers;

import java.util.List;
import java.util.Optional;

public interface ITraitementServ {

    List<traitement_fichiers> getAllTraitements();

    Optional<traitement_fichiers> getTraitementById(Long id);

    traitement_fichiers createTraitement(traitement_fichiers traitement);

    traitement_fichiers updateTraitement(Long id, traitement_fichiers updatedTraitement);

    void deleteTraitement(Long id);
    void processFile(MultipartFile file);
}
