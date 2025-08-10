package tn.esprit.ruya.Carthago.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.ruya.Carthago.repository.ICarthagoRepo;
import tn.esprit.ruya.Fichier.repository.IFichierrepo;
import tn.esprit.ruya.models.Carthago;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.user.repository.IUserRepo;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataInitializerCarthago {

    private final Random random = new Random();

    private String getCodeValeur(String type) {
        return switch (type.toLowerCase()) {
            case "cheque" -> String.valueOf(30 + random.nextInt(4));  // 30 → 33
            case "effet" -> String.valueOf(40 + random.nextInt(2));   // 40 or 41
            case "virement" -> "10";
            case "prelevement" -> "20";
            default -> "00";
        };
    }

    @Bean
    CommandLineRunner fillFakeData(IFichierrepo fichierRepo, ICarthagoRepo carthagoRepo, IUserRepo userRepo) {
        return args -> {
            User user = userRepo.findById(1L).orElse(null);
            if (user == null) {
                System.out.println("⚠️ Aucun utilisateur trouvé !");
                return;
            }

            String[] types = {"cheque", "effet", "virement", "prelevement"};
            String[] natures = {"env", "rcp"};
            String[] sensList = {"emis", "recu"};

            // Remplir FICHIER
            if (fichierRepo.count() == 0) {
                for (int i = 0; i < 20; i++) {
                    String type = types[i % types.length];
                    Fichier f = new Fichier();
                    f.setNomFichier("fichier_" + type + "_" + String.format("%04d", i + 1));
                    f.setTypeFichier(type);
                    f.setNatureFichier(natures[i % natures.length]);
                    f.setSens(sensList[i % sensList.length]);
                    f.setCodeValeur(getCodeValeur(type));
                    f.setCodEn("21");
                    f.setMontant(5000.0 + (i * 1000));
                    f.setNomber(5 + i);
                    f.setCreatedAt(LocalDateTime.now());
                    f.setUser(user);
                    fichierRepo.save(f);
                }
                System.out.println("✅ Table FICHIER remplie");
            }

            // Remplir CARTHAGO
            if (carthagoRepo.count() == 0) {
                for (int i = 0; i < 20; i++) {
                    String type = types[i % types.length];
                    Carthago c = new Carthago();
                    c.setNomFichier("carthago_" + type + "_" + String.format("%04d", i + 1));
                    c.setTypeFichier(type);
                    c.setNatureFichier(natures[i % natures.length]);
                    c.setSens(sensList[i % sensList.length]);
                    c.setCodeValeur(getCodeValeur(type));
                    c.setCodEn("22");
                    c.setMontant(7000.0 + (i * 800));
                    c.setNomber(3 + i);
                    c.setCreatedAt(LocalDateTime.now());
                    c.setUser(user);
                    carthagoRepo.save(c);
                }
                System.out.println("✅ Table CARTHAGO remplie");
            }
        };
    }
}
