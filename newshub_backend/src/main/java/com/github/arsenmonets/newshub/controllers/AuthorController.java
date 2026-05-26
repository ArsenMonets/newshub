package com.github.arsenmonets.newshub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.NewsDTO;
import com.github.arsenmonets.newshub.dto.NewsInputDTO;
import com.github.arsenmonets.newshub.services.NewsService;

@RestController
@RequestMapping("/api/v1/author")
public class AuthorController {

    private final NewsService newsService;

    public AuthorController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping("/news")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsDTO createNews(@RequestBody NewsInputDTO data,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return newsService.create(data, userDetails);
    }

    @PutMapping("/news/{id}")
    public NewsDTO updateNews(@PathVariable Long id, @RequestBody NewsInputDTO data,
            @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        return newsService.update(id, data, userDetails);
    }

    @DeleteMapping("/news/{id}")
    public String deleteNews(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUserDTO userDetails) {
        newsService.delete(id, userDetails);
        return "Новину успішно видалено";
    }
}
