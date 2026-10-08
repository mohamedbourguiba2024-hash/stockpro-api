package com.stockpro.api.service;

import com.stockpro.api.dto.*;
import com.stockpro.api.entity.*;
import com.stockpro.api.exception.*;
import com.stockpro.api.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public MovementResponse create(MovementRequest r, String userEmail) {
        Product product = productRepository.findByIdForUpdate(r.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit introuvable (id=" + r.productId() + ")"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        int current = product.getQuantite();
        int newQty;
        if (r.type() == MovementType.SORTIE) {
            if (r.quantite() > current) {
                throw new InsufficientStockException("Stock insuffisant : " + current
                        + " disponible(s), " + r.quantite() + " demandé(s)");
            }
            newQty = current - r.quantite();
        } else {
            newQty = current + r.quantite();
        }
        product.setQuantite(newQty);

        StockMovement m = StockMovement.builder()
                .product(product).type(r.type()).quantite(r.quantite())
                .commentaire(r.commentaire()).user(user).build();
        return toResponse(movementRepository.save(m), newQty);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovementResponse> search(Long productId, MovementType type,
                                                 LocalDate from, LocalDate to,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "date", "id"));

        Specification<StockMovement> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (productId != null) ps.add(cb.equal(root.get("product").get("id"), productId));
            if (type != null) ps.add(cb.equal(root.get("type"), type));
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get("date"), from.atStartOfDay()));
            if (to != null) ps.add(cb.lessThan(root.get("date"), to.plusDays(1).atStartOfDay()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return PageResponse.from(movementRepository.findAll(spec, pageable)
                .map(m -> toResponse(m, null)));
    }

    private MovementResponse toResponse(StockMovement m, Integer stockApres) {
        Product p = m.getProduct();
        return new MovementResponse(m.getId(), p.getId(), p.getReference(), p.getNom(),
                m.getType(), m.getQuantite(), m.getDate(), m.getCommentaire(),
                m.getUser().getEmail(), stockApres);
    }
}