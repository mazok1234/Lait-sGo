package com.example.demo.services.vente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.production.Production;
import com.example.demo.entity.vente.RefProduit;
import com.example.demo.entity.vente.Vente;
import com.example.demo.repository.auth.UtilisateurRepository;
import com.example.demo.repository.production.ProductionRepository;
import com.example.demo.repository.vente.RefProduitRepository;
import com.example.demo.repository.vente.VenteRepository;
import com.example.demo.services.alerte.AlerteService;
import com.example.demo.services.cheptel.StatutVieService;

@Service
@Transactional
public class VenteService {

    private static final String CODE_LAIT = "LAIT";
    private static final String CODE_VEAU = "VEAU";
    private static final String CODE_VACHE = "VACHE";
    private static final String STATUT_VIE_VEAU = "Veau";
    private static final String STATUT_VIE_VACHE_ACTIVE = "Vache_active";

    private final ProductionRepository productionRepository;
    private final VenteRepository venteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RefProduitRepository refProduitRepository;
    private final AlerteService alerteService;
    private final StatutVieService statutVieService;

    private static final BigDecimal SEUIL_STOCK_LAIT_L = new BigDecimal("50");

    private static final BigDecimal PRIX_MAX_LAIT = new BigDecimal("9999.99");

    public VenteService(ProductionRepository productionRepository,
            VenteRepository venteRepository,
            UtilisateurRepository utilisateurRepository,
            RefProduitRepository refProduitRepository,
            AlerteService alerteService,
            StatutVieService statutVieService) {
        this.productionRepository = productionRepository;
        this.venteRepository = venteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.refProduitRepository = refProduitRepository;
        this.alerteService = alerteService;
        this.statutVieService = statutVieService;
    }

    private int nombreDisponible(String statutVieLibelle) {
        return statutVieService.findVacheIdsByStatut(statutVieService.getByLibelle(statutVieLibelle).getId()).size();
    }

    public List<RefProduit> getProduits() {
        return refProduitRepository.findAllByOrderByLibelleAsc();
    }

    public Vente findById(Integer id) {
        return venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente introuvable : " + id));
    }

    public Page<Vente> findByPrixTotalBetweenDate(
            BigDecimal min, BigDecimal max, Integer produitId, String order, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return venteRepository.findByPrixTotalBetweenDateVenteDesc(
                min, max, produitId, PageRequest.of(page, size, Sort.by(direction, "dateVente")));
    }
 
    public Vente effectuerVente(BigDecimal quantite, BigDecimal prixUnitaire, LocalDate dateVente, RefProduit produit) {
        if (produit == null || produit.getId() == null) {
            throw new RuntimeException("Le produit vendu doit être sélectionné.");
        }
        RefProduit produitResolu = refProduitRepository.findById(produit.getId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable."));
        String unite = produitResolu.getUniteDefaut();

        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le prix unitaire doit être supérieur à 0.");
        }
        boolean estLait = CODE_LAIT.equals(produitResolu.getCode());
        if (estLait && prixUnitaire.compareTo(PRIX_MAX_LAIT) > 0) {
            throw new RuntimeException("Le prix unitaire ne peut pas dépasser 9 999,99 Ar/" + unite + ".");
        }
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La quantité doit être supérieure à 0.");
        }
        if (dateVente == null || dateVente.isAfter(LocalDate.now())) {
            throw new RuntimeException("La date de vente ne peut pas être dans le futur.");
        }

        if (estLait) {
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
        }

        if (CODE_VEAU.equals(produitResolu.getCode())) {
            int disponible = nombreDisponible(STATUT_VIE_VEAU);
            if (quantite.compareTo(BigDecimal.valueOf(disponible)) > 0) {
                throw new RuntimeException("Stock insuffisant. Veaux disponibles dans le cheptel : " + disponible);
            }
        }

        if (CODE_VACHE.equals(produitResolu.getCode())) {
            int disponible = nombreDisponible(STATUT_VIE_VACHE_ACTIVE);
            if (quantite.compareTo(BigDecimal.valueOf(disponible)) > 0) {
                throw new RuntimeException("Stock insuffisant. Vaches disponibles dans le cheptel : " + disponible);
            }
        }

        Vente vente = new Vente();
        vente.setDateVente(dateVente);
        vente.setProduit(produitResolu);
        vente.setQuantite(quantite);
        vente.setPrixUnitaire(prixUnitaire);
        vente.setCreatedAt(LocalDateTime.now());
        vente.setCreatedBy(resolveCreateurVente());

        Vente saved = venteRepository.save(vente);

        if (estLait) {
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
                alerteService.acquitterAutomatiquement("stock_lait_bas", null);
            }
        }

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
    BigDecimal veauxDisponibles = BigDecimal.valueOf(nombreDisponible(STATUT_VIE_VEAU));
    BigDecimal vachesDisponibles = BigDecimal.valueOf(nombreDisponible(STATUT_VIE_VACHE_ACTIVE));

    for (Vente vente : ventes) {

        if (vente.getProduit() == null || vente.getProduit().getCode() == null) {
            throw new RuntimeException(
                    "Produit invalide pour la vente du "
                    + vente.getDateVente()
            );
        }

        if (vente.getPrixUnitaire() == null ||
                vente.getPrixUnitaire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Prix invalide pour la vente du "
                    + vente.getDateVente()
            );
        }

        if (vente.getQuantite() == null ||
                vente.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
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

        if (CODE_LAIT.equals(vente.getProduit().getCode())) {
            if (stockDisponible.compareTo(vente.getQuantite()) < 0) {
                throw new RuntimeException(
                        "Stock insuffisant pour la vente du "
                        + vente.getDateVente()
                        + ". Stock restant : "
                        + stockDisponible
                        + " L"
                );
            }

            stockDisponible = stockDisponible.subtract(vente.getQuantite());
        }

        if (CODE_VEAU.equals(vente.getProduit().getCode())) {
            if (veauxDisponibles.compareTo(vente.getQuantite()) < 0) {
                throw new RuntimeException(
                        "Stock insuffisant pour la vente du "
                        + vente.getDateVente()
                        + ". Veaux disponibles : "
                        + veauxDisponibles
                );
            }

            veauxDisponibles = veauxDisponibles.subtract(vente.getQuantite());
        }

        if (CODE_VACHE.equals(vente.getProduit().getCode())) {
            if (vachesDisponibles.compareTo(vente.getQuantite()) < 0) {
                throw new RuntimeException(
                        "Stock insuffisant pour la vente du "
                        + vente.getDateVente()
                        + ". Vaches disponibles : "
                        + vachesDisponibles
                );
            }

            vachesDisponibles = vachesDisponibles.subtract(vente.getQuantite());
        }
    }

    for (Vente vente : ventes) {
        effectuerVente(
                vente.getQuantite(),
                vente.getPrixUnitaire(),
                vente.getDateVente(),
                vente.getProduit()
        );
    }
}

}