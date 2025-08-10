package tn.esprit.ruya.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Notification;

import java.util.List;

@Repository
public interface INotificationRepo extends JpaRepository<Notification, Long> {
    
    // Récupérer toutes les notifications non lues
    List<Notification> findByLuOrderByTimestampDesc(Boolean lu);
    
    // Récupérer toutes les notifications par type
    List<Notification> findByTypeOrderByTimestampDesc(Notification.NotificationType type);
    
    // Récupérer toutes les notifications ordonnées par timestamp
    List<Notification> findAllByOrderByTimestampDesc();
    
    // Compter les notifications non lues
    Long countByLu(Boolean lu);
} 