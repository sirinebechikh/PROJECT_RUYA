package tn.esprit.ruya.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.Notification;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.notification.repository.INotificationRepo;

import java.util.List;

@AllArgsConstructor
@Service
public class NotificationService {

    private INotificationRepo notificationRepo;

    // Créer une notification pour l'ajout d'un fichier
    public Notification creerNotificationAjout(Fichier fichier, User userAction) {
        System.out.println("🔍 DEBUG - Création de notification d'ajout pour fichier: " + fichier.getNomFichier());
        System.out.println("🔍 DEBUG - Utilisateur qui a ajouté: " + userAction.getUsername());

        String titre = "Nouveau fichier " + fichier.getTypeFichier();
        String message = "Le fichier \"" + fichier.getNomFichier() + "\" a été ajouté par " + userAction.getUsername() + ".";
        String icon = getFileTypeIcon(fichier.getTypeFichier());

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.AJOUT);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setFichier(fichier);
        notification.setUserAction(userAction);
        notification.setIcon(icon);
        notification.setLu(false);

        Notification savedNotification = notificationRepo.save(notification);
        System.out.println("🔍 DEBUG - Notification créée avec ID: " + savedNotification.getId());
        
        return savedNotification;
    }

    // Récupérer toutes les notifications
    public List<Notification> getAllNotifications() {
        return notificationRepo.findAllByOrderByTimestampDesc();
    }

    // Récupérer les notifications non lues
    public List<Notification> getNotificationsNonLues() {
        return notificationRepo.findByLuOrderByTimestampDesc(false);
    }

    // Marquer une notification comme lue
    public Notification marquerCommeLue(Long notificationId) {
        return notificationRepo.findById(notificationId).map(notification -> {
            notification.setLu(true);
            return notificationRepo.save(notification);
        }).orElse(null);
    }

    // Marquer toutes les notifications comme lues
    public void marquerToutesCommeLues() {
        List<Notification> notificationsNonLues = getNotificationsNonLues();
        notificationsNonLues.forEach(notification -> {
            notification.setLu(true);
            notificationRepo.save(notification);
        });
    }

    // Compter les notifications non lues
    public Long countNotificationsNonLues() {
        return notificationRepo.countByLu(false);
    }

    // Obtenir l'icône selon le type de fichier
    private String getFileTypeIcon(String typeFichier) {
        switch (typeFichier.toLowerCase()) {
            case "cheque":
                return "ti ti-receipt";
            case "effet":
                return "ti ti-file-text";
            case "virement":
                return "ti ti-exchange";
            case "prelevement":
                return "ti ti-arrow-down";
            default:
                return "ti ti-file";
        }
    }
} 