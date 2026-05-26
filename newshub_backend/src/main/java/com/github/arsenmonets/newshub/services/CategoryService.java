package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.exceptions.ResourceNotFoundException;
import com.github.arsenmonets.newshub.exceptions.ResourceAlreadyExistsException;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.NewsRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsHubMapper mapper;
    private static final String DEFAULT_CATEGORY_NAME = "DEFAULT";

    public CategoryService(CategoryRepository categoryRepository, NewsRepository newsRepository,
            UserRepository userRepository, NewsHubMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(mapper::toCategoryDTO)
                .toList();
    }

    public CategoryDTO read(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорія не знайдена: " + id));
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO add(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Категорія з такою назвою вже існує");
        }
        CategoryEntity category = new CategoryEntity(name);
        return mapper.toCategoryDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, String name) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорія не знайдена: " + id));
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Категорія з такою назвою вже існує");
        }
        category.setName(name);
        return mapper.toCategoryDTO(categoryRepository.save(category));
    }

    @Transactional
    public void remove(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категорія не знайдена: " + id);
        }
        CategoryEntity defaultCategory = categoryRepository.findByName(DEFAULT_CATEGORY_NAME)
                .orElseGet(() -> {
                    CategoryEntity newDefault = new CategoryEntity(DEFAULT_CATEGORY_NAME);
                    return categoryRepository.save(newDefault);
                });
        newsRepository.updateNewsCategoryId(id, defaultCategory.getId());
        userRepository.removeUserCategorySubscription(id);
        categoryRepository.deleteById(id);
    }
}
