package com.stockpro.api.service;

import com.stockpro.api.dto.CategoryRequest;
import com.stockpro.api.dto.CategoryResponse;
import com.stockpro.api.entity.Category;
import com.stockpro.api.exception.DuplicateResourceException;
import com.stockpro.api.exception.ResourceNotFoundException;
import com.stockpro.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String nom = request.nom().trim();
        if (repository.existsByNom(nom)) {
            throw new DuplicateResourceException("Une catégorie nommée '" + nom + "' existe déjà");
        }
        return toResponse(repository.save(Category.builder().nom(nom).build()));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        String nom = request.nom().trim();
        if (repository.existsByNomAndIdNot(nom, id)) {
            throw new DuplicateResourceException("Une catégorie nommée '" + nom + "' existe déjà");
        }
        category.setNom(nom);
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getOrThrow(id);
        repository.delete(category);
        repository.flush(); // déclenche l'erreur tout de suite si la catégorie est utilisée
    }

    private Category getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable (id=" + id + ")"));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getNom());
    }
}