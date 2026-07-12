package com.example.demo.services.vente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.production.Production;
import com.example.demo.entity.vente.Vente;
import com.example.demo.repository.auth.UtilisateurRepository;
import com.example.demo.repository.production.ProductionRepository;
import com.example.demo.repository.vente.VenteRepository;
import com.example.demo.services.alerte.AlerteService;

@Service
@Transactional
public class VenteService {

    private final ProductionRepository productionRepository;
    private final VenteRepository venteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AlerteService alerteService; // ← ajouté

    // Seuil en litres sous lequel une alerte stock lait est envoyée
    private static final BigDecimal SEUIL_STOCK_LAIT_L = new BigDecimal("50");

    public VenteService(ProductionRepository productionRepository,
            VenteRepository venteRepository,
            UtilisateurRepository utilisateurRepository,
            AlerteService alerteService) { // ← ajouté
        this.productionRepository = productionRepository;
        this.venteRepository = venteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.alerteService = alerteService; // ← ajouté
    }

    // 1. findById — l'id est Integer dans Vente
    public Vente findById(Integer id) {
        return venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente introuvable : " + id));
    }

    // 2. getOldestVentes — tri par date ASC avec pagination
    public Page<Vente> getOldestVentes(int page, int size) {
        return venteRepository.findAllByOrderByDateVenteAsc(
                PageRequest.of(page, size));
    }

    // 3. findByPrixTotalBetweenDateDesc — déjà dans le repository, juste exposer
    public Page<Vente> findByPrixTotalBetweenDateDesc(
            BigDecimal min, BigDecimal max, int page, int size) {
        return venteRepository.findByPrixTotalBetweenDateDesc(
                min, max, PageRequest.of(page, size));
    }

    public Vente effectuerVente(BigDecimal quantite, BigDecimal prixUnitaire, LocalDate dateVente) {
        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le prix unitaire doit être supérieur à 0.");
        }
        BigDecimal maxPrix = new BigDecimal("9999.99");
        if (prixUnitaire.compareTo(maxPrix) > 0) {
            throw new RuntimeException("Le prix unitaire ne peut pas dépasser 9 999,99 Ar/L.");
        }
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La quantité doit être supérieure à 0.");
        }
        if (dateVente == null || dateVente.isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de vente ne peut pas être dans le futur.");
        }

        BigDecimal stock = productionRepository.getRemainingStock();
        if (stock.compareTo(quantite) < 0) {
            throw new RuntimeException("Stock insuffisant. Stock actuel : " + stock + " L");
        }

        List<Production> productions = productionRepository
                .findByQuantiteRestanteGreaterThanOrderByDateProductionAsc(BigDecimal.ZERO);
        BigDecimal restant = quantite;
        List<Production> modified = new ArrayList<>();

        for (Production production : productions) {
            if (restant.compareTo(BigDecimal.ZERO) <= 0)
                break;
            BigDecimal disponible = production.getQuantiteRestante();
            if (disponible.compareTo(restant) >= 0) {
                production.setQuantiteRestante(disponible.subtract(restant));
                restant = BigDecimal.ZERO;
            } else {
                restant = restant.subtract(disponible);
                production.setQuantiteRestante(BigDecimal.ZERO);
            }
            modified.add(production);
        }
        productionRepository.saveAll(modified);

        Vente vente = new Vente();
        vente.setDateVente(dateVente);
        vente.setQuantiteLait(quantite);
        vente.setPrixUnitaire(prixUnitaire);
        vente.setCreatedAt(LocalDateTime.now());
        vente.setCreatedBy(resolveCreateurVente());

        Vente saved = venteRepository.save(vente);

        // ← INJECTION ALERTE — vérification du stock après vente
        BigDecimal stockRestant = productionRepository.getRemainingStock();
        if (stockRestant.compareTo(SEUIL_STOCK_LAIT_L) <= 0) {
            alerteService.envoyerAlerte(
                    "stock_lait_bas",
                    "attention",
                    "Stock de lait insuffisant",
                    "Stock restant après vente du " + dateVente
                            + " : " + stockRestant + " L"
                            + " (seuil critique : " + SEUIL_STOCK_LAIT_L + " L).",
                    null);
        } else {
            // Stock reconstitué au-dessus du seuil → acquittement automatique
            alerteService.acquitterAutomatiquement("stock_lait_bas", null);
        }
        // ← FIN INJECTION

        return saved;
    }

    public void genererAlerteStockLait() {
        BigDecimal stockRestant = productionRepository.getRemainingStock();
        if (stockRestant == null) {
            return;
        }
        if (stockRestant.compareTo(SEUIL_STOCK_LAIT_L) <= 0) {
            alerteService.envoyerAlerte(
                    "stock_lait_bas",
                    "attention",
                    "Stock de lait insuffisant",
                    "Stock actuel : " + stockRestant + " L",
                    null);
        } else {
            alerteService.acquitterAutomatiquement("stock_lait_bas", null);
        }
    }

    private com.example.demo.entity.auth.Utilisateur resolveCreateurVente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (username != null && !username.isBlank() && !"anonymousUser".equalsIgnoreCase(username)) {
                var byEmail = utilisateurRepository.findByEmail(username);
                if (byEmail.isPresent()) {
                    return byEmail.get();
                }
            }
        }

        return utilisateurRepository.findById(1L)
                .orElse(utilisateurRepository.findAll(PageRequest.of(0, 1))
                        .stream()
                        .findFirst()
                        .orElse(null));
    }

@Transactional
public void importerVentes(List<Vente> ventes) {

    BigDecimal stockDisponible = productionRepository.getRemainingStock();

    for (Vente vente : ventes) {

        if (vente.getPrixUnitaire() == null ||
                vente.getPrixUnitaire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Prix invalide pour la vente du "
                    + vente.getDateVente()
            );
        }

        if (vente.getQuantiteLait() == null ||
                vente.getQuantiteLait().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Quantité invalide pour la vente du "
                    + vente.getDateVente()
            );
        }

        if (vente.getDateVente() == null ||
                vente.getDateVente().isAfter(LocalDate.now())) {
            throw new RuntimeException(
                    "Date invalide pour la vente du "
                    + vente.getDateVente()
            );
        }

        if (stockDisponible.compareTo(
                vente.getQuantiteLait()) < 0) {

            throw new RuntimeException(
                    "Stock insuffisant pour la vente du "
                    + vente.getDateVente()
                    + ". Stock restant : "
                    + stockDisponible
                    + " L"
            );
        }

        stockDisponible =
                stockDisponible.subtract(
                        vente.getQuantiteLait()
                );
    }

    for (Vente vente : ventes) {
        effectuerVente(
                vente.getQuantiteLait(),
                vente.getPrixUnitaire(),
                vente.getDateVente()
        );
    }
}

}