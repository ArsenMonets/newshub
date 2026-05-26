package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.exceptions.ResourceAlreadyExistsException;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.NewsRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.arsenmonets.newshub.exceptions.ResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsHubMapper mapper;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryEntity category;
    private CategoryEntity defaultCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = new CategoryEntity("Technology");
        category.setId(1L);

        defaultCategory = new CategoryEntity("DEFAULT");
        defaultCategory.setId(99L);
    }

    @Test
    @DisplayName("Should get all categories successfully")
    void testGetAllSuccess() {
        List<CategoryEntity> categories = Arrays.asList(category, defaultCategory);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "Technology"));
        when(mapper.toCategoryDTO(defaultCategory)).thenReturn(new CategoryDTO(99L, "DEFAULT"));

        List<CategoryDTO> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void testGetAllEmpty() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList());

        List<CategoryDTO> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should add new category successfully")
    void testAddSuccess() {
        when(categoryRepository.existsByName("Technology")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(category);
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "Technology"));

        categoryService.add("Technology");

        verify(categoryRepository).existsByName("Technology");
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate category")
    void testAddFailsDuplicate() {
        when(categoryRepository.existsByName("Technology")).thenReturn(true);

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class,
                () -> categoryService.add("Technology"));
        assertTrue(ex.getMessage().contains("вже існує"));

        verify(categoryRepository).existsByName("Technology");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove category and reassign news to DEFAULT category")
    void testRemoveSuccess() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByName("DEFAULT")).thenReturn(Optional.of(defaultCategory));
        doNothing().when(newsRepository).updateNewsCategoryId(1L, 99L);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.remove(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).findByName("DEFAULT");
        verify(newsRepository).updateNewsCategoryId(1L, 99L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should create DEFAULT category if it doesn't exist during removal")
    void testRemoveCreatesDefaultCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByName("DEFAULT")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(defaultCategory);
        doNothing().when(newsRepository).updateNewsCategoryId(1L, 99L);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.remove(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).findByName("DEFAULT");
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(newsRepository).updateNewsCategoryId(1L, 99L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent category")
    void testRemoveFailsNotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.remove(999L));

        verify(categoryRepository).existsById(999L);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should update category successfully")
    void testUpdateSuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Sport")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(category);
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "Technology"));

        categoryService.update(1L, "Sport");

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Sport");
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to existing category name")
    void testUpdateFailsDuplicate() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Sport")).thenReturn(true);

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class,
                () -> categoryService.update(1L, "Sport"));
        assertTrue(ex.getMessage().contains("вже існує"));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Sport");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void testUpdateFailsNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update(999L, "Sport"));

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow update with same name")
    void testUpdateWithSameName() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(category);
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "Technology"));

        categoryService.update(1L, "Technology");

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should read category successfully")
    void testReadSuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "Technology"));

        CategoryDTO result = categoryService.read(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Technology", result.name());
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when reading non-existent category")
    void testReadFailsNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.read(999L));

        verify(categoryRepository).findById(999L);
    }

    @Test
    @DisplayName("Should add category and map to DTO")
    void testAddCategoryAndMapDTO() {
        CategoryEntity newCategory = new CategoryEntity("Sport");
        newCategory.setId(2L);

        when(categoryRepository.existsByName("Sport")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(newCategory);
        when(mapper.toCategoryDTO(newCategory)).thenReturn(new CategoryDTO(2L, "Sport"));

        CategoryDTO result = categoryService.add("Sport");

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("Sport", result.name());
        verify(categoryRepository).existsByName("Sport");
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(mapper).toCategoryDTO(newCategory);
    }

    @Test
    @DisplayName("Should handle concurrent update correctly")
    void testUpdateDoesNotAffectOtherCategories() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("NewTech")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(category);
        when(mapper.toCategoryDTO(category)).thenReturn(new CategoryDTO(1L, "NewTech"));

        CategoryDTO result = categoryService.update(1L, "NewTech");

        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("NewTech");
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should validate category exists before removal")
    void testRemoveValidatesExistence() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.remove(999L));

        verify(categoryRepository).existsById(999L);
        verify(categoryRepository, never()).findByName("DEFAULT");
        verify(newsRepository, never()).updateNewsCategoryId(any(), any());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should return correct count of categories")
    void testGetAllMultipleCategories() {
        CategoryEntity cat1 = new CategoryEntity("Tech");
        cat1.setId(1L);
        CategoryEntity cat2 = new CategoryEntity("Sport");
        cat2.setId(2L);
        CategoryEntity cat3 = new CategoryEntity("Health");
        cat3.setId(3L);

        List<CategoryEntity> categories = Arrays.asList(cat1, cat2, cat3);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(mapper.toCategoryDTO(cat1)).thenReturn(new CategoryDTO(1L, "Tech"));
        when(mapper.toCategoryDTO(cat2)).thenReturn(new CategoryDTO(2L, "Sport"));
        when(mapper.toCategoryDTO(cat3)).thenReturn(new CategoryDTO(3L, "Health"));

        List<CategoryDTO> result = categoryService.getAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(categoryRepository).findAll();
        verify(mapper, times(3)).toCategoryDTO(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should remove user category subscriptions during category deletion")
    void testRemoveDeletesUserCategorySubscriptions() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByName("DEFAULT")).thenReturn(Optional.of(defaultCategory));
        doNothing().when(userRepository).removeUserCategorySubscription(1L);
        doNothing().when(newsRepository).updateNewsCategoryId(1L, 99L);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.remove(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).findByName("DEFAULT");
        verify(userRepository).removeUserCategorySubscription(1L);
        verify(newsRepository).updateNewsCategoryId(1L, 99L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should remove user subscriptions even when creating default category")
    void testRemoveDeletesUserSubscriptionsWithDefaultCategoryCreation() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByName("DEFAULT")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(defaultCategory);
        doNothing().when(userRepository).removeUserCategorySubscription(1L);
        doNothing().when(newsRepository).updateNewsCategoryId(1L, 99L);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.remove(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).findByName("DEFAULT");
        verify(categoryRepository).save(any(CategoryEntity.class));
        verify(userRepository).removeUserCategorySubscription(1L);
        verify(newsRepository).updateNewsCategoryId(1L, 99L);
        verify(categoryRepository).deleteById(1L);
    }
}
