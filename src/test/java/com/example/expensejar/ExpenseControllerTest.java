package com.example.expensejar;

import com.example.expensejar.controller.ExpenseController;
import com.example.expensejar.dto.BudgetAlertResponse;
import com.example.expensejar.dto.CategorySummaryResponse;
import com.example.expensejar.dto.MonthlyTrendResponse;
import com.example.expensejar.exception.GlobalExceptionHandler;
import com.example.expensejar.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@Import(GlobalExceptionHandler.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Test
    @DisplayName("POST /api/expenses - Successful expense creation with 201 Created and BudgetAlertResponse")
    void testAddExpenseSuccess() throws Exception {
        BudgetAlertResponse alertResponse = new BudgetAlertResponse(
                "Expense added successfully", "Food", 3000.0, 5000.0, 60.0, false
        );

        when(expenseService.addExpense(any())).thenReturn(alertResponse);

        String jsonBody = """
                {
                  "userId": 1,
                  "categoryId": 1,
                  "amount": 250.00,
                  "date": "2026-09-28"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Expense added successfully")))
                .andExpect(jsonPath("$.category", is("Food")))
                .andExpect(jsonPath("$.totalSpent", is(3000.0)))
                .andExpect(jsonPath("$.budget", is(5000.0)))
                .andExpect(jsonPath("$.percentageUsed", is(60.0)))
                .andExpect(jsonPath("$.alert", is(false)));
    }

    @Test
    @DisplayName("POST /api/expenses - Reject negative amount with 400 Bad Request")
    void testAddExpenseNegativeAmountRejected() throws Exception {
        String jsonBody = """
                {
                  "userId": 1,
                  "categoryId": 1,
                  "amount": -100.00,
                  "date": "2026-09-28"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Expense amount must be greater than zero")));
    }

    @Test
    @DisplayName("POST /api/expenses - Reject zero amount with 400 Bad Request")
    void testAddExpenseZeroAmountRejected() throws Exception {
        String jsonBody = """
                {
                  "userId": 1,
                  "categoryId": 1,
                  "amount": 0.00,
                  "date": "2026-09-28"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Expense amount must be greater than zero")));
    }

    @Test
    @DisplayName("GET /api/expenses/summary/current-month/{userId} - 200 OK with summary list")
    void testGetCurrentMonthSummary() throws Exception {
        when(expenseService.getCurrentMonthCategorySummary(1L)).thenReturn(Arrays.asList(
                new CategorySummaryResponse("Food", 3200.0),
                new CategorySummaryResponse("Travel", 1800.0)
        ));

        mockMvc.perform(get("/api/expenses/summary/current-month/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", is("Food")))
                .andExpect(jsonPath("$[0].totalSpent", is(3200.0)))
                .andExpect(jsonPath("$[1].category", is("Travel")))
                .andExpect(jsonPath("$[1].totalSpent", is(1800.0)));
    }

    @Test
    @DisplayName("GET /api/expenses/trend/{userId} - 200 OK with trend list")
    void testGetMonthOverMonthTrend() throws Exception {
        when(expenseService.getMonthOverMonthTrend(1L)).thenReturn(Arrays.asList(
                new MonthlyTrendResponse("2026-07", 6200.0),
                new MonthlyTrendResponse("2026-08", 7100.0),
                new MonthlyTrendResponse("2026-09", 5800.0)
        ));

        mockMvc.perform(get("/api/expenses/trend/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month", is("2026-07")))
                .andExpect(jsonPath("$[0].totalSpent", is(6200.0)))
                .andExpect(jsonPath("$[1].month", is("2026-08")))
                .andExpect(jsonPath("$[1].totalSpent", is(7100.0)))
                .andExpect(jsonPath("$[2].month", is("2026-09")))
                .andExpect(jsonPath("$[2].totalSpent", is(5800.0)));
    }
}
