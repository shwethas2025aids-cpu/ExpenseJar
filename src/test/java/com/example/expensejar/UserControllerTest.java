package com.example.expensejar;

import com.example.expensejar.controller.UserController;
import com.example.expensejar.dto.UserRequest;
import com.example.expensejar.entity.User;
import com.example.expensejar.exception.GlobalExceptionHandler;
import com.example.expensejar.service.UserService;
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

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users - 201 Created")
    void testCreateUser() throws Exception {
        User createdUser = new User(1L, "Sakthi", "sakthi@example.com");
        when(userService.createUser(any(UserRequest.class))).thenReturn(createdUser);

        String jsonBody = """
                {
                  "name": "Sakthi",
                  "email": "sakthi@example.com"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Sakthi")))
                .andExpect(jsonPath("$.email", is("sakthi@example.com")));
    }

    @Test
    @DisplayName("POST /api/users - Invalid email returns 400 Bad Request")
    void testCreateUserInvalidEmail() throws Exception {
        String jsonBody = """
                {
                  "name": "Sakthi",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users - 200 OK")
    void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(
                new User(1L, "Sakthi", "sakthi@example.com")
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Sakthi")));
    }
}
