package com.stockpro.api.service;

import com.stockpro.api.dto.PageResponse;
import com.stockpro.api.dto.ProductRequest;
import com.stockpro.api.dto.ProductResponse;
import com.stockpro.api.entity.Category;
import com.stockpro.api.entity.Product;
import com.stockpro.api.entity.Supplier;
import com.stockpro.api.exception.DuplicateResourceException;
import com.stockpro.api.exception.ResourceNotFoundException;
import com.stockpro.api.repository.CategoryRepository;
import com.stockpro.api.repository.ProductRepository;
import com.stockpro.api.repository.SupplierRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Set<String> SORTABLE = Set.of("id", "nom", "reference", "prix", "quantite");

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, Long categoryId, Boolean lowStock,
                                                int page, int size, String sort, String dir) {
        String sortField = SORTABLE.contains(sort) ? sort : "nom";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(direction, sortField));

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nom")), like),
                        cb.like(cb.lower(root.get("reference")), like)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (Boolean.TRUE.equals(lowStock)) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.<Integer>get("quantite"), root.<Integer>get("seuilAlerte")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return PageResponse.from(repository.findAll(spec, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return repository.findLowStock().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest r) {
        String reference = r.reference().trim();
        if (repository.existsByReference(reference)) {
            throw new DuplicateResourceException("La référence '" + reference + "' existe déjà");
        }
        Product p = new Product();
        apply(p, r);
        p.setReference(reference);
        p.setQuantite(r.quantite()); // quantité initiale, ensuite modifiée uniquement par les mouvements
        return toResponse(repository.save(p));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest r) {
        Product p = getOrThrow(id);
        String reference = r.reference().trim();
        if (repository.existsByReferenceAndIdNot(reference, id)) {
            throw new DuplicateResourceException("La référence '" + reference + "' existe déjà");
        }
        apply(p, r);
        p.setReference(reference);
        // la quantité n'est volontairement PAS modifiée ici : on passe par les mouvements de stock
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush(); // erreur 409 si des mouvements existent pour ce produit
    }

    private void apply(Product p, ProductRequest r) {
        p.setNom(r.nom().trim());
        p.setDescription(r.description());
        p.setPrix(r.prix());
        p.setSeuilAlerte(r.seuilAlerte());

        Category category = null;
        if (r.categoryId() != null) {
            category = categoryRepository.findById(r.categoryId()).orElseThrow(() ->
                    new ResourceNotFoundException("Catégorie introuvable (id=" + r.categoryId() + ")"));
        }
        p.setCategory(category);

        Supplier supplier = null;
        if (r.supplierId() != null) {
            supplier = supplierRepository.findById(r.supplierId()).orElseThrow(() ->
                    new ResourceNotFoundException("Fournisseur introuvable (id=" + r.supplierId() + ")"));
        }
        p.setSupplier(supplier);
    }

    private Product getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Produit introuvable (id=" + id + ")"));
    }

    private ProductResponse toResponse(Product p) {
        Category c = p.getCategory();
        Supplier s = p.getSupplier();
        return new ProductResponse(p.getId(), p.getReference(), p.getNom(), p.getDescription(),
                p.getPrix(), p.getQuantite(), p.getSeuilAlerte(),
                p.getQuantite() <= p.getSeuilAlerte(),
                c != null ? c.getId() : null, c != null ? c.getNom() : null,
                s != null ? s.getId() : null, s != null ? s.getNom() : null);
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM UTF-8 pour Excel
        sb.append("Référence;Nom;Catégorie;Fournisseur;Prix;Quantité;Seuil d'alerte;Stock bas\n");
        for (Product p : repository.findAll(Sort.by("nom"))) {
            sb.append(csv(p.getReference())).append(';')
                    .append(csv(p.getNom())).append(';')
                    .append(csv(p.getCategory() != null ? p.getCategory().getNom() : "")).append(';')
                    .append(csv(p.getSupplier() != null ? p.getSupplier().getNom() : "")).append(';')
                    .append(p.getPrix().toPlainString().replace('.', ',')).append(';')
                    .append(p.getQuantite()).append(';')
                    .append(p.getSeuilAlerte()).append(';')
                    .append(p.getQuantite() <= p.getSeuilAlerte() ? "Oui" : "Non").append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        // protection contre l'injection de formules dans Excel
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v + "\"";
    }
}