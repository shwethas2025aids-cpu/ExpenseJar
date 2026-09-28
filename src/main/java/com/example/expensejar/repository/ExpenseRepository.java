package com.example.expensejar.repository;

import com.example.expensejar.dto.CategorySummaryResponse;
import com.example.expensejar.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findByUserIdOrderByDateAsc(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.category.id = :categoryId " +
           "AND e.date BETWEEN :startDate AND :endDate")
    Double calculateTotalSpentByCategoryAndDateRange(@Param("userId") Long userId,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT new com.example.expensejar.dto.CategorySummaryResponse(e.category.name, SUM(e.amount)) " +
           "FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category.name")
    List<CategorySummaryResponse> findCategorySummaryForDateRange(@Param("userId") Long userId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);
}
