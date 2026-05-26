package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.SubscriptionsDTO;
import com.github.arsenmonets.newshub.services.UserService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/subscribe/category/{categoryId}")
    public SubscriptionsDTO subscribeToCategory(@PathVariable Long categoryId,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return userService.subscribeToCategory(userDetails, categoryId);
    }

    @PostMapping("/subscribe/author/{authorId}")
    public SubscriptionsDTO subscribeToAuthor(@PathVariable Long authorId,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return userService.subscribeToAuthor(userDetails, authorId);
    }

    @DeleteMapping("/unsubscribe/category/{categoryId}")
    public SubscriptionsDTO unsubscribeFromCategory(@PathVariable Long categoryId,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return userService.unsubscribeFromCategory(userDetails, categoryId);
    }

    @DeleteMapping("/unsubscribe/author/{authorId}")
    public SubscriptionsDTO unsubscribeFromAuthor(@PathVariable Long authorId,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return userService.unsubscribeFromAuthor(userDetails, authorId);
    }

    @GetMapping("/subscriptions")
    public SubscriptionsDTO getMySubscriptions(@AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return userService.getSubscriptions(userDetails);
    }
}
