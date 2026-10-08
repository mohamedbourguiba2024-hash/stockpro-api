package com.stockpro.api.service;

import com.stockpro.api.dto.SupplierRequest;
import com.stockpro.api.dto.SupplierResponse;
import com.stockpro.api.entity.Supplier;
import com.stockpro.api.exception.ResourceNotFoundException;
import com.stockpro.api.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        apply(supplier, request);
        return toResponse(repository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = getOrThrow(id);
        apply(supplier, request);
        return toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = getOrThrow(id);
        repository.delete(supplier);
        repository.flush();
    }

    private void apply(Supplier s, SupplierRequest r) {
        s.setNom(r.nom().trim());
        s.setTelephone(r.telephone());
        s.setEmail(r.email());
        s.setAdresse(r.adresse());
    }

    private Supplier getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable (id=" + id + ")"));
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(s.getId(), s.getNom(), s.getTelephone(), s.getEmail(), s.getAdresse());
    }
}