package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.NewsEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.NewsRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("PublicController Integration Tests")
@TestPropertySource(locations = "classpath:application-integration.properties")
class PublicControllerIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsRepository newsRepository;

    private static final String PUBLIC_NEWS_SEARCH_URL = "/api/v1/public/news/search";
    private static final String PUBLIC_NEWS_FILTER_URL = "/api/v1/public/news/filter";
    private static final String PUBLIC_CATEGORIES_URL = "/api/v1/public/categories";
    private static final String PUBLIC_NEWS_DETAILS_URL = "/api/v1/public/news/{id}";
    private static final String PUBLIC_AUTHORS_URL = "/api/v1/public/authors";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        newsRepository.deleteAll();
    }

    @Test
    @DisplayName("Get all categories success")
    void getAllCategoriesSuccess() throws Exception {
        CategoryEntity category1 = new CategoryEntity("Technology");
        CategoryEntity category2 = new CategoryEntity("Sports");
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        mockMvc.perform(get(PUBLIC_CATEGORIES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Sports"))
                .andExpect(jsonPath("$[1].name").value("Technology"));
    }

    @Test
    @DisplayName("Get all categories returns empty list")
    void getAllCategoriesReturnsEmptyList() throws Exception {
        mockMvc.perform(get(PUBLIC_CATEGORIES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Get category by id success")
    void getCategoryByIdSuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        CategoryEntity saved = categoryRepository.save(category);

        mockMvc.perform(get("/api/v1/public/categories/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Technology"));
    }

    @Test
    @DisplayName("Get category by id not found")
    void getCategoryByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/public/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get all authors success")
    void getAllAuthorsSuccess() throws Exception {
        UserEntity author1 = new UserEntity("author1", "author1@test.com", "password", UserRole.AUTHOR);
        author1.setBlocked(false);
        UserEntity author2 = new UserEntity("author2", "author2@test.com", "password", UserRole.AUTHOR);
        author2.setBlocked(false);
        userRepository.save(author1);
        userRepository.save(author2);

        mockMvc.perform(get(PUBLIC_AUTHORS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].login").value("author1"))
                .andExpect(jsonPath("$[1].login").value("author2"));
    }

    @Test
    @DisplayName("Get all authors returns empty list when no authors")
    void getAllAuthorsReturnsEmptyList() throws Exception {
        mockMvc.perform(get(PUBLIC_AUTHORS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Get news details success")
    void getNewsDetailsSuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        categoryRepository.save(category);

        UserEntity author = new UserEntity("author", "author@test.com", "password", UserRole.AUTHOR);
        author.setBlocked(false);
        userRepository.save(author);

        NewsEntity news = new NewsEntity();
        news.setTitle("Test News");
        news.setContent("Test Content");
        news.setCategory(category);
        news.setAuthor(author);
        news.setCreatedAt(LocalDateTime.now());
        NewsEntity saved = newsRepository.save(news);

        mockMvc.perform(get(PUBLIC_NEWS_DETAILS_URL, saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Test News"))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    @Test
    @DisplayName("Get news details not found")
    void getNewsDetailsNotFound() throws Exception {
        mockMvc.perform(get(PUBLIC_NEWS_DETAILS_URL, 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Search news by query success")
    void searchNewsByQuerySuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        categoryRepository.save(category);

        UserEntity author = new UserEntity("author", "author@test.com", "password", UserRole.AUTHOR);
        author.setBlocked(false);
        userRepository.save(author);

        NewsEntity news = new NewsEntity();
        news.setTitle("Spring Boot Tutorial");
        news.setContent("Learn Spring Boot");
        news.setCategory(category);
        news.setAuthor(author);
        news.setCreatedAt(LocalDateTime.now());
        newsRepository.save(news);

        mockMvc.perform(get(PUBLIC_NEWS_SEARCH_URL)
                .param("query", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Tutorial"));
    }

    @Test
    @DisplayName("Search news returns empty results")
    void searchNewsReturnsEmptyResults() throws Exception {
        mockMvc.perform(get(PUBLIC_NEWS_SEARCH_URL)
                .param("query", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Filter news by category success")
    void filterNewsByCategorySuccess() throws Exception {
        CategoryEntity tech = new CategoryEntity("Technology");
        CategoryEntity sports = new CategoryEntity("Sports");
        tech = categoryRepository.save(tech);
        sports = categoryRepository.save(sports);

        UserEntity author = new UserEntity("author", "author@test.com", "password", UserRole.AUTHOR);
        author.setBlocked(false);
        userRepository.save(author);

        NewsEntity techNews = new NewsEntity();
        techNews.setTitle("Tech News");
        techNews.setContent("Tech Content");
        techNews.setCategory(tech);
        techNews.setAuthor(author);
        techNews.setCreatedAt(LocalDateTime.now());
        newsRepository.save(techNews);

        mockMvc.perform(get(PUBLIC_NEWS_FILTER_URL)
                .param("categoryIds", tech.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Tech News"));
    }

    @Test
    @DisplayName("Filter news by author success")
    void filterNewsByAuthorSuccess() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        categoryRepository.save(category);

        UserEntity author1 = new UserEntity("author1", "author1@test.com", "password", UserRole.AUTHOR);
        author1.setBlocked(false);
        UserEntity author2 = new UserEntity("author2", "author2@test.com", "password", UserRole.AUTHOR);
        author2.setBlocked(false);
        author1 = userRepository.save(author1);
        author2 = userRepository.save(author2);

        NewsEntity news1 = new NewsEntity();
        news1.setTitle("News from Author1");
        news1.setContent("Content");
        news1.setCategory(category);
        news1.setAuthor(author1);
        news1.setCreatedAt(LocalDateTime.now());
        newsRepository.save(news1);

        mockMvc.perform(get(PUBLIC_NEWS_FILTER_URL)
                .param("authorIds", author1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("News from Author1"));
    }

    @Test
    @DisplayName("Filter news returns empty results")
    void filterNewsReturnsEmptyResults() throws Exception {
        mockMvc.perform(get(PUBLIC_NEWS_FILTER_URL)
                .param("categoryIds", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Filter news with pagination")
    void filterNewsWithPagination() throws Exception {
        CategoryEntity category = new CategoryEntity("Technology");
        categoryRepository.save(category);

        UserEntity author = new UserEntity("author", "author@test.com", "password", UserRole.AUTHOR);
        author.setBlocked(false);
        userRepository.save(author);

        for (int i = 0; i < 3; i++) {
            NewsEntity news = new NewsEntity();
            news.setTitle("News " + i);
            news.setContent("Content " + i);
            news.setCategory(category);
            news.setAuthor(author);
            news.setCreatedAt(LocalDateTime.now());
            newsRepository.save(news);
        }

        mockMvc.perform(get(PUBLIC_NEWS_FILTER_URL)
                .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.number").value(0));
    }
}
