package com.example.expensejar.repository;

import com.example.expensejar.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByCategoryId(Long categoryId);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(Long userId, Long categoryId, String month);
}
