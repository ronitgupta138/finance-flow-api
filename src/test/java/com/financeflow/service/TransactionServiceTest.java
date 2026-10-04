package com.financeflow.service;

import com.financeflow.dto.TransactionRequest;
import com.financeflow.dto.TransactionResponse;
import com.financeflow.entity.Category;
import com.financeflow.entity.Role;
import com.financeflow.entity.Transaction;
import com.financeflow.entity.TransactionType;
import com.financeflow.entity.User;
import com.financeflow.repository.CategoryRepository;
import com.financeflow.repository.TransactionRepository;
import com.financeflow.repository.UserRepository;
import com.financeflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private UserPrincipal principal;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User("ronit@dev.io", "hashedPass", "Ronit Gupta", Role.ROLE_USER);
        user.setId(1L);
        principal = UserPrincipal.create(user);

        category = new Category("Cloud Infrastructure", "#0891b2", BigDecimal.valueOf(500), user);
        category.setId(10L);
    }

    @Test
    void testCreateTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(49.99));
        request.setType(TransactionType.EXPENSE);
        request.setDescription("AWS Server Hosting");
        request.setTransactionDate(LocalDate.now());
        request.setCategoryId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(category));

        Transaction savedTx = new Transaction(
                request.getAmount(), request.getType(), request.getDescription(),
                request.getTransactionDate(), category, user
        );
        savedTx.setId(100L);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(request, principal);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(49.99), response.getAmount());
        assertEquals("AWS Server Hosting", response.getDescription());
        assertEquals("Cloud Infrastructure", response.getCategoryName());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
