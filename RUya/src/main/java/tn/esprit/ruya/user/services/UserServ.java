package tn.esprit.ruya.user.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.ConfirmResetCodeDto;
import tn.esprit.ruya.models.RoleUser;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.user.repository.IUserRepo;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserServ implements IUserServ {

    private final IUserRepo userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        user.setRole(RoleUser.SIMPLE_USER);
        user.setIsActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 🔐 Hachage
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            return userRepository.save(user);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsernameOrEmail(String input) {
        if (input.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return userRepository.findByEmail(input);
        } else {
            return userRepository.findByUsername(input);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Mémoire temporaire
    private final Map<String, String> emailToResetCode = new HashMap<>();
    private final Map<String, String> codeToEmail = new HashMap<>();

    public ResponseEntity<?> generateAndSendResetCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Utilisateur non trouvé"));
        }

        String resetCode = String.format("%06d", new Random().nextInt(1_000_000));
        emailToResetCode.put(email, resetCode);
        codeToEmail.put(resetCode, email);
        sendResetCodeByEmail(email, resetCode);

        return ResponseEntity.ok(Map.of("message", "Code de réinitialisation envoyé"));
    }

    public ResponseEntity<?> confirmResetCode(ConfirmResetCodeDto dto) {
        String email = codeToEmail.get(dto.getResetCode());

        if (email == null || !emailToResetCode.get(email).equals(dto.getResetCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Code invalide ou expiré"));
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Utilisateur non trouvé"));
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword())); // 🔐 Hachage
        userRepository.save(user);

        emailToResetCode.remove(email);
        codeToEmail.remove(dto.getResetCode());

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }

    private void sendResetCodeByEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Votre code de vérification RU'ya");

            String htmlContent = createSimpleEmailTemplate(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    private String createSimpleEmailTemplate(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body{margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;background-color:#f7f8fa;color:#001e00}" +
                ".container{max-width:600px;margin:0 auto;background-color:#ffffff}" +
                ".header{padding:32px 40px;border-bottom:1px solid #e4e6ea}" +
                ".logo{font-size:24px;font-weight:700;color:#a85d3b;-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;margin:0}" +
                ".content{padding:40px}" +
                ".title{font-size:20px;font-weight:600;color:#001e00;margin:0 0 24px 0;line-height:1.3}" +
                ".text{font-size:16px;color:#001e00;line-height:1.5;margin:0 0 24px 0}" +
                ".code-box{background-color:#f7f8fa;border:2px solid #14a800;border-radius:8px;padding:24px;text-align:center;margin:32px 0}" +
                ".code-label{font-size:14px;color:#5e6d55;margin-bottom:8px;font-weight:500}" +
                ".verification-code{font-size:32px;font-weight:700;color:#14a800;font-family:monospace;letter-spacing:4px}" +
                ".note{background-color:#fff4e6;border-left:4px solid #ff6b35;padding:16px;margin:24px 0;font-size:14px;color:#8b4513}" +
                ".footer{padding:32px 40px;border-top:1px solid #e4e6ea;background-color:#f7f8fa}" +
                ".footer-text{font-size:14px;color:#5e6d55;margin:0;line-height:1.4}" +
                ".divider{height:1px;background-color:#e4e6ea;margin:24px 0}" +
                "@media only screen and (max-width:600px){" +
                ".header,.content,.footer{padding:24px 20px}" +
                ".verification-code{font-size:28px;letter-spacing:2px}" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img src='https://upload.wikimedia.org/wikipedia/commons/f/fc/Logo_Attijari_bank.png' alt='RUya Logo' style='height:40px;margin-bottom:8px;'/>" +
                "<h1 class='logo' style='margin-top:8px;'>RU'ya</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2 class='title'>Votre code de vérification</h2>" +
                "<p class='text'>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte. Utilisez le code de vérification ci-dessous pour continuer :</p>" +
                "<div class='code-box'>" +
                "<div class='code-label'>CODE DE VÉRIFICATION</div>" +
                "<div class='verification-code'>" + code + "</div>" +
                "</div>" +
                "<div class='note'>" +
                "<strong>Important :</strong> Ce code expire dans 10 minutes. Ne le partagez avec personne." +
                "</div>" +
                "<p class='text'>Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.</p>" +
                "<div class='divider'></div>" +
                "<p class='text' style='color:#5e6d55;font-size:14px;'>Besoin d'aide ? Contactez notre équipe support.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p class='footer-text'>Cet email a été envoyé par RU'ya. Merci de ne pas répondre à cet email.</p>" +
                "<p class='footer-text' style='margin-top:8px;'>© 2025 RU'ya. Tous droits réservés.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public User updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(active);
        return userRepository.save(user);
    }
}