package com.stockpro.api.service;

import com.stockpro.api.dto.MovementRequest;
import com.stockpro.api.dto.MovementResponse;
import com.stockpro.api.entity.*;
import com.stockpro.api.exception.InsufficientStockException;
import com.stockpro.api.exception.ResourceNotFoundException;
import com.stockpro.api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock StockMovementRepository movementRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks StockMovementService service;

    Product product;
    User user;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).reference("BOI-001").nom("Eau")
                .prix(BigDecimal.ONE).quantite(10).seuilAlerte(5).build();
        user = User.builder().id(1L).email("admin@stockpro.com").build();
    }

    @Test
    void entree_augmenteLeStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("admin@stockpro.com")).thenReturn(Optional.of(user));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        MovementResponse res = service.create(
                new MovementRequest(1L, MovementType.ENTREE, 5, null), "admin@stockpro.com");

        assertEquals(15, product.getQuantite());
        assertEquals(15, res.stockApres());
    }

    @Test
    void sortie_diminueLeStock() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("admin@stockpro.com")).thenReturn(Optional.of(user));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        service.create(new MovementRequest(1L, MovementType.SORTIE, 4, null), "admin@stockpro.com");

        assertEquals(6, product.getQuantite());
    }

    @Test
    void sortie_exacteJusquaZero_estAutorisee() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("admin@stockpro.com")).thenReturn(Optional.of(user));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        service.create(new MovementRequest(1L, MovementType.SORTIE, 10, null), "admin@stockpro.com");

        assertEquals(0, product.getQuantite());
    }

    @Test
    void sortie_superieureAuStock_estRefusee_etRienNestEnregistre() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("admin@stockpro.com")).thenReturn(Optional.of(user));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> service.create(new MovementRequest(1L, MovementType.SORTIE, 11, null),
                        "admin@stockpro.com"));

        assertTrue(ex.getMessage().contains("10"));
        assertEquals(10, product.getQuantite());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void produitInconnu_leveUneException() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(new MovementRequest(99L, MovementType.ENTREE, 1, null),
                        "admin@stockpro.com"));
        verify(movementRepository, never()).save(any());
    }
}