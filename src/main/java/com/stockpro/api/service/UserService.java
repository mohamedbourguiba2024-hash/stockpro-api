package com.stockpro.api.service;

import com.stockpro.api.dto.*;
import com.stockpro.api.entity.User;
import com.stockpro.api.exception.DuplicateResourceException;
import com.stockpro.api.exception.ResourceNotFoundException;
import com.stockpro.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest r) {
        String email = r.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new DuplicateResourceException("Un utilisateur avec l'email '" + email + "' existe déjà");
        }
        User u = User.builder().nom(r.nom().trim()).email(email)
                .motDePasse(encoder.encode(r.password())).role(r.role()).build();
        return toResponse(repository.save(u));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest r, String currentEmail) {
        User u = getOrThrow(id);
        boolean self = u.getEmail().equalsIgnoreCase(currentEmail);
        if (self && r.role() != u.getRole()) {
            throw new DuplicateResourceException("Vous ne pouvez pas modifier votre propre rôle");
        }
        u.setNom(r.nom().trim());
        u.setRole(r.role());
        if (r.password() != null && !r.password().isBlank()) {
            u.setMotDePasse(encoder.encode(r.password()));
        }
        return toResponse(u);
    }

    @Transactional
    public void delete(Long id, String currentEmail) {
        User u = getOrThrow(id);
        if (u.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new DuplicateResourceException("Vous ne pouvez pas supprimer votre propre compte");
        }
        repository.delete(u);
        repository.flush(); // 409 si l'utilisateur a des mouvements de stock
    }

    private User getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Utilisateur introuvable (id=" + id + ")"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getNom(), u.getEmail(), u.getRole());
    }
}