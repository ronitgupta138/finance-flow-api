package com.financeflow.service;

import com.financeflow.dto.CategoryRequest;
import com.financeflow.dto.CategoryResponse;
import com.financeflow.entity.Category;
import com.financeflow.entity.User;
import com.financeflow.exception.BadRequestException;
import com.financeflow.exception.ResourceNotFoundException;
import com.financeflow.repository.CategoryRepository;
import com.financeflow.repository.UserRepository;
import com.financeflow.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(UserPrincipal principal) {
        return categoryRepository.findAllByUserId(principal.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id, UserPrincipal principal) {
        Category category = categoryRepository.findByIdAndUserId(id, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, UserPrincipal principal) {
        if (categoryRepository.existsByNameAndUserId(request.getName().trim(), principal.getId())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category(
                request.getName().trim(),
                request.getColor() != null ? request.getColor() : "#0891b2",
                request.getMonthlyBudget(),
                user
        );

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCategory(Long id, UserPrincipal principal) {
        Category category = categoryRepository.findByIdAndUserId(id, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getMonthlyBudget(),
                category.getCreatedAt()
        );
    }
}
