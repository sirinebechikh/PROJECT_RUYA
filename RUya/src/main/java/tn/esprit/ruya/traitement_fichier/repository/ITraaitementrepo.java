package tn.esprit.ruya.traitement_fichier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.traitement_fichiers;

@Repository
public interface ITraaitementrepo extends JpaRepository<traitement_fichiers, Long> {
}
