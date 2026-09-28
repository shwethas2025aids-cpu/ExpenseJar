package com.example.expensejar;

import com.example.expensejar.controller.BudgetController;
import com.example.expensejar.dto.BudgetRequest;
import com.example.expensejar.entity.Budget;
import com.example.expensejar.entity.Category;
import com.example.expensejar.entity.User;
import com.example.expensejar.exception.GlobalExceptionHandler;
import com.example.expensejar.service.BudgetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@Import(GlobalExceptionHandler.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @Test
    @DisplayName("POST /api/budgets - 201 Created")
    void testSetBudget() throws Exception {
        User user = new User(1L, "Sakthi", "sakthi@example.com");
        Category category = new Category(1L, "Food");
        Budget budget = new Budget(1L, 5000.0, "2026-09", user, category);

        when(budgetService.setBudget(any(BudgetRequest.class))).thenReturn(budget);

        String jsonBody = """
                {
                  "userId": 1,
                  "categoryId": 1,
                  "amount": 5000.00,
                  "month": "2026-09"
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(5000.0)))
                .andExpect(jsonPath("$.month", is("2026-09")));
    }
}
