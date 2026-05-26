package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.services.CategoryService;
import com.github.arsenmonets.newshub.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CategoryService categoryService;
    private final UserService userService;

    public AdminController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public Page<UserDTO> getAllNonAdminUsers(
            @RequestParam(defaultValue = "") String loginFilter,
            Pageable pageable) {
        return userService.getAllNonAdminsWithLoginFilter(loginFilter, pageable);
    }

    @PostMapping("/users/{userId}/block")
    public UserDTO blockUser(@PathVariable Long userId) {
        return userService.updateUserStatus(userId, true);
    }

    @PostMapping("/users/{userId}/unblock")
    public UserDTO unblockUser(@PathVariable Long userId) {
        return userService.updateUserStatus(userId, false);
    }

    @PutMapping("/users/{userId}/role")
    public UserDTO changeUserRole(@PathVariable Long userId, @RequestParam String newRole) {
        return userService.changeRole(userId, newRole);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDTO addCategory(@RequestParam String name) {
        return categoryService.add(name);
    }

    @PutMapping("/categories/{id}")
    public CategoryDTO updateCategory(@PathVariable Long id, @RequestParam String name) {
        return categoryService.update(id, name);
    }

    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.remove(id);
        return "Категорію успішно видалено";
    }
}
