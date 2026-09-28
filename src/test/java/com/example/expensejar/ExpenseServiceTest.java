package com.example.expensejar;

import com.example.expensejar.dto.BudgetAlertResponse;
import com.example.expensejar.dto.CategorySummaryResponse;
import com.example.expensejar.dto.ExpenseRequest;
import com.example.expensejar.dto.MonthlyTrendResponse;
import com.example.expensejar.entity.Budget;
import com.example.expensejar.entity.Category;
import com.example.expensejar.entity.Expense;
import com.example.expensejar.entity.User;
import com.example.expensejar.exception.InvalidExpenseException;
import com.example.expensejar.repository.BudgetRepository;
import com.example.expensejar.repository.CategoryRepository;
import com.example.expensejar.repository.ExpenseRepository;
import com.example.expensejar.repository.UserRepository;
import com.example.expensejar.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User sampleUser;
    private Category foodCategory;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Sakthi", "sakthi@example.com");
        foodCategory = new Category(1L, "Food");
        sampleBudget = new Budget(1L, 5000.0, "2026-09", sampleUser, foodCategory);
    }

    @Test
    @DisplayName("RULE 1: Reject negative expense amount with InvalidExpenseException")
    void testNegativeExpenseAmountRejected() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, -100.0, LocalDate.of(2026, 9, 28));

        InvalidExpenseException exception = assertThrows(InvalidExpenseException.class, () -> {
            expenseService.addExpense(request);
        });

        assertEquals("Expense amount must be greater than zero", exception.getMessage());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("RULE 1: Reject zero expense amount with InvalidExpenseException")
    void testZeroExpenseAmountRejected() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, 0.0, LocalDate.of(2026, 9, 28));

        InvalidExpenseException exception = assertThrows(InvalidExpenseException.class, () -> {
            expenseService.addExpense(request);
        });

        assertEquals("Expense amount must be greater than zero", exception.getMessage());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    @DisplayName("RULE 2: Budget alert triggers when spending reaches or exceeds 90%")
    void testBudgetAlertTriggersWhenExceeding90Percent() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, 1600.0, LocalDate.of(2026, 9, 28));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Suppose total spending for September is 4600 out of 5000 budget (92%)
        when(expenseRepository.calculateTotalSpentByCategoryAndDateRange(
                eq(1L), eq(1L), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30))))
                .thenReturn(4600.0);

        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09"))
                .thenReturn(Optional.of(sampleBudget));

        BudgetAlertResponse response = expenseService.addExpense(request);

        assertNotNull(response);
        assertEquals("Budget alert", response.getMessage());
        assertEquals("Food", response.getCategory());
        assertEquals(4600.0, response.getTotalSpent());
        assertEquals(5000.0, response.getBudget());
        assertEquals(92.0, response.getPercentageUsed());
        assertTrue(response.isAlert());
    }

    @Test
    @DisplayName("RULE 2: No alert when spending is below 90% of budget")
    void testNoAlertWhenSpendingBelow90Percent() {
        ExpenseRequest request = new ExpenseRequest(1L, 1L, 250.0, LocalDate.of(2026, 9, 28));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(foodCategory));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Spending is 3000 out of 5000 budget (60%)
        when(expenseRepository.calculateTotalSpentByCategoryAndDateRange(
                eq(1L), eq(1L), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 30))))
                .thenReturn(3000.0);

        when(budgetRepository.findByUserIdAndCategoryIdAndMonth(1L, 1L, "2026-09"))
                .thenReturn(Optional.of(sampleBudget));

        BudgetAlertResponse response = expenseService.addExpense(request);

        assertNotNull(response);
        assertEquals("Expense added successfully", response.getMessage());
        assertEquals("Food", response.getCategory());
        assertEquals(3000.0, response.getTotalSpent());
        assertEquals(5000.0, response.getBudget());
        assertEquals(60.0, response.getPercentageUsed());
        assertFalse(response.isAlert());
    }

    @Test
    @DisplayName("Current month category summary retrieves accurate totals")
    void testCurrentMonthCategorySummary() {
        when(userRepository.existsById(1L)).thenReturn(true);
        List<CategorySummaryResponse> mockSummary = Arrays.asList(
                new CategorySummaryResponse("Food", 3200.0),
                new CategorySummaryResponse("Travel", 1800.0),
                new CategorySummaryResponse("Stationery", 750.0)
        );

        when(expenseRepository.findCategorySummaryForDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockSummary);

        List<CategorySummaryResponse> result = expenseService.getCurrentMonthCategorySummary(1L);

        assertEquals(3, result.size());
        assertEquals("Food", result.get(0).getCategory());
        assertEquals(3200.0, result.get(0).getTotalSpent());
        assertEquals("Travel", result.get(1).getCategory());
        assertEquals(1800.0, result.get(1).getTotalSpent());
        assertEquals("Stationery", result.get(2).getCategory());
        assertEquals(750.0, result.get(2).getTotalSpent());
    }

    @Test
    @DisplayName("Month-over-month trend groups and aggregates spending correctly")
    void testMonthOverMonthTrend() {
        when(userRepository.existsById(1L)).thenReturn(true);

        Expense e1 = new Expense(1L, 6200.0, LocalDate.of(2026, 7, 15), sampleUser, foodCategory);
        Expense e2 = new Expense(2L, 7100.0, LocalDate.of(2026, 8, 10), sampleUser, foodCategory);
        Expense e3 = new Expense(3L, 3000.0, LocalDate.of(2026, 9, 5), sampleUser, foodCategory);
        Expense e4 = new Expense(4L, 2800.0, LocalDate.of(2026, 9, 20), sampleUser, foodCategory);

        when(expenseRepository.findByUserIdOrderByDateAsc(1L)).thenReturn(Arrays.asList(e1, e2, e3, e4));

        List<MonthlyTrendResponse> trend = expenseService.getMonthOverMonthTrend(1L);

        assertEquals(3, trend.size());
        assertEquals("2026-07", trend.get(0).getMonth());
        assertEquals(6200.0, trend.get(0).getTotalSpent());

        assertEquals("2026-08", trend.get(1).getMonth());
        assertEquals(7100.0, trend.get(1).getTotalSpent());

        assertEquals("2026-09", trend.get(2).getMonth());
        assertEquals(5800.0, trend.get(2).getTotalSpent());
    }
}
