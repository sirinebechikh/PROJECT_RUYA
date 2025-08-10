package tn.esprit.ruya.notification.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.Notification;
import tn.esprit.ruya.notification.service.NotificationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private NotificationService notificationService;

    // Récupérer toutes les notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    // Récupérer les notifications non lues
    @GetMapping("/non-lues")
    public ResponseEntity<List<Notification>> getNotificationsNonLues() {
        return ResponseEntity.ok(notificationService.getNotificationsNonLues());
    }

    // Marquer une notification comme lue
    @PutMapping("/{id}/marquer-lue")
    public ResponseEntity<Notification> marquerCommeLue(@PathVariable Long id) {
        Notification notification = notificationService.marquerCommeLue(id);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Marquer toutes les notifications comme lues
    @PutMapping("/marquer-toutes-lues")
    public ResponseEntity<Void> marquerToutesCommeLues() {
        notificationService.marquerToutesCommeLues();
        return ResponseEntity.ok().build();
    }

    // Compter les notifications non lues
    @GetMapping("/count-non-lues")
    public ResponseEntity<Long> countNotificationsNonLues() {
        return ResponseEntity.ok(notificationService.countNotificationsNonLues());
    }
} 