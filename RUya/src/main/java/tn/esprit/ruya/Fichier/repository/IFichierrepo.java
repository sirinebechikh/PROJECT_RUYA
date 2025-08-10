package tn.esprit.ruya.Fichier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Fichier;

import java.util.List;


@Repository
public interface IFichierrepo extends JpaRepository<Fichier, Long> {

    List<Fichier> findByUserId(Long userId);

}
