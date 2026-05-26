package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.dto.NewsDTO;
import com.github.arsenmonets.newshub.dto.NewsPreviewDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.services.CategoryService;
import com.github.arsenmonets.newshub.services.NewsService;
import com.github.arsenmonets.newshub.services.UserService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final CategoryService categoryService;
    private final NewsService newsService;
    private final UserService userService;

    public PublicController(CategoryService categoryService, NewsService newsService, UserService userService) {
        this.categoryService = categoryService;
        this.newsService = newsService;
        this.userService = userService;
    }

    @GetMapping("/news/search")
    public Page<NewsPreviewDTO> searchNews(@RequestParam String query, @RequestParam(defaultValue = "0") int page) {
        return newsService.search(query, page);
    }

    @GetMapping("/news/filter")
    public Page<NewsPreviewDTO> filterNews(
            @RequestParam(required = false, defaultValue = "") List<Long> categoryIds,
            @RequestParam(required = false, defaultValue = "") List<Long> authorIds,
            @RequestParam(defaultValue = "0") int page) {
        return newsService.filter(categoryIds, authorIds, page);
    }

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAll();
    }

    @GetMapping("/categories/{id}")
    public CategoryDTO getCategory(@PathVariable Long id) {
        return categoryService.read(id);
    }

    @GetMapping("/news/{id}")
    public NewsDTO getNewsDetails(@PathVariable Long id) {
        return newsService.read(id);
    }

    @GetMapping("/authors")
    public List<UserDTO> getAllAuthors() {
        return userService.getAllAuthors();
    }
}
