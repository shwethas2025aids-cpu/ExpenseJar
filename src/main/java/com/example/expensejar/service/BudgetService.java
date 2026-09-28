package com.example.expensejar.service;

import com.example.expensejar.dto.BudgetRequest;
import com.example.expensejar.entity.Budget;
import com.example.expensejar.entity.Category;
import com.example.expensejar.entity.User;
import com.example.expensejar.exception.ResourceNotFoundException;
import com.example.expensejar.repository.BudgetRepository;
import com.example.expensejar.repository.CategoryRepository;
import com.example.expensejar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Budget setBudget(BudgetRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndCategoryIdAndMonth(
                request.getUserId(), request.getCategoryId(), request.getMonth());

        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setAmount(request.getAmount());
        } else {
            budget = new Budget(request.getAmount(), request.getMonth(), user, category);
        }

        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return budgetRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return budgetRepository.findByCategoryId(categoryId);
    }

    public void deleteBudget(Long id) {
        Budget budget = getBudgetById(id);
        budgetRepository.delete(budget);
    }
}
