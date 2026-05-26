package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.NewsDTO;
import com.github.arsenmonets.newshub.dto.NewsInputDTO;
import com.github.arsenmonets.newshub.dto.NewsPreviewDTO;
import com.github.arsenmonets.newshub.dto.UserDTO;
import com.github.arsenmonets.newshub.dto.CategoryDTO;
import com.github.arsenmonets.newshub.exceptions.CustomAccessDeniedException;
import com.github.arsenmonets.newshub.exceptions.ResourceNotFoundException;
import com.github.arsenmonets.newshub.models.CategoryEntity;
import com.github.arsenmonets.newshub.models.NewsEntity;
import com.github.arsenmonets.newshub.models.UserEntity;
import com.github.arsenmonets.newshub.models.UserRole;
import com.github.arsenmonets.newshub.repositories.CategoryRepository;
import com.github.arsenmonets.newshub.repositories.NewsRepository;
import com.github.arsenmonets.newshub.repositories.UserRepository;
import com.github.arsenmonets.newshub.utils.NewsHubMapper;
import com.github.arsenmonets.newshub.websocket.NewsWebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("NewsService Tests")
class NewsServiceTest {

        @Mock
        private NewsRepository newsRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private NewsWebSocketController newsWebSocketController;

        @Mock
        private NewsHubMapper mapper;

        @InjectMocks
        private NewsService newsService;

        private UserEntity author;
        private CategoryEntity category;
        private NewsEntity news;
        private NewsInputDTO newsInputDTO;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                author = new UserEntity("author1", "author@example.com", "password", UserRole.AUTHOR);
                author.setId(1L);

                category = new CategoryEntity("Technology");
                category.setId(1L);

                newsInputDTO = new NewsInputDTO("Test Title", "Test Content", 1L);

                news = new NewsEntity("Test Title", "Test Content", author, category);
                news.setId(1L);
                news.setCreatedAt(LocalDateTime.now());

                when(userRepository.findById(1L)).thenReturn(Optional.of(author));
                when(userRepository.save(any(UserEntity.class))).thenReturn(author);
        }

        @Test
        @DisplayName("Should search news by query successfully")
        void testSearchSuccess() {
                String query = "Java";
                List<NewsEntity> newsList = Arrays.asList(news);
                Page<NewsEntity> pageResult = new PageImpl<>(newsList);

                when(newsRepository.searchByKeyword(eq(query), any(Pageable.class)))
                                .thenReturn(pageResult);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);

                Page<NewsPreviewDTO> result = newsService.search(query, 0);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(newsRepository).searchByKeyword(eq(query), any(Pageable.class));
                verify(mapper).toNewsPreviewDTO(news);
        }

        @Test
        @DisplayName("Should return empty page when no news found")
        void testSearchEmptyResult() {
                String query = "NonExistent";
                Page<NewsEntity> pageResult = new PageImpl<>(Arrays.asList());

                when(newsRepository.searchByKeyword(eq(query), any(Pageable.class)))
                                .thenReturn(pageResult);

                Page<NewsPreviewDTO> result = newsService.search(query, 0);

                assertNotNull(result);
                assertEquals(0, result.getTotalElements());
                verify(newsRepository).searchByKeyword(eq(query), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter news by categories and authors successfully")
        void testFilterSuccess() {
                List<Long> categoryIds = Arrays.asList(1L);
                List<Long> authorIds = Arrays.asList(1L);
                List<NewsEntity> newsList = Arrays.asList(news);
                Page<NewsEntity> pageResult = new PageImpl<>(newsList);

                when(newsRepository.filter(eq(categoryIds), eq(authorIds), any(Pageable.class)))
                                .thenReturn(pageResult);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);

                Page<NewsPreviewDTO> result = newsService.filter(categoryIds, authorIds, 0);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(newsRepository).filter(eq(categoryIds), eq(authorIds), any(Pageable.class));
                verify(mapper).toNewsPreviewDTO(news);
        }

        @Test
        @DisplayName("Should filter news with empty category list")
        void testFilterWithEmptyCategories() {
                List<Long> categoryIds = Arrays.asList();
                List<Long> authorIds = Arrays.asList(1L);
                List<NewsEntity> newsList = Arrays.asList(news);
                Page<NewsEntity> pageResult = new PageImpl<>(newsList);

                when(newsRepository.filter(eq(categoryIds), eq(authorIds), any(Pageable.class)))
                                .thenReturn(pageResult);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);

                Page<NewsPreviewDTO> result = newsService.filter(categoryIds, authorIds, 0);

                assertNotNull(result);
                verify(newsRepository).filter(eq(categoryIds), eq(authorIds), any(Pageable.class));
                verify(mapper).toNewsPreviewDTO(news);
        }

        @Test
        @DisplayName("Should create news successfully")
        void testCreateSuccess() {
                when(categoryRepository.getReferenceById(1L)).thenReturn(category);
                when(userRepository.getReferenceById(1L)).thenReturn(author);
                when(newsRepository.save(any(NewsEntity.class))).thenReturn(news);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);
                when(mapper.toNewsDTO(news)).thenReturn(newsDTO);

                when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
                when(mapper.toNewsDTO(any(NewsEntity.class))).thenReturn(newsDTO);

                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                newsService.create(newsInputDTO, authorDetails);

                verify(categoryRepository).findById(1L);
                verify(userRepository).getReferenceById(1L);
                verify(newsRepository).save(any(NewsEntity.class));
                verify(mapper).toNewsPreviewDTO(news);
                verify(mapper).toNewsDTO(news);
                verify(newsWebSocketController).sendNewsCreate(previewDTO);
        }

        @Test
        @DisplayName("Should read news by id successfully")
        void testReadSuccess() {
                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsDTO(news)).thenReturn(newsDTO);

                NewsDTO result = newsService.read(1L);

                assertNotNull(result);
                assertEquals("Test Title", result.title());
                verify(newsRepository).findById(1L);
                verify(mapper).toNewsDTO(news);
        }

        @Test
        @DisplayName("Should throw exception when reading non-existent news")
        void testReadFailsWithInvalidId() {
                when(newsRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class,
                                () -> newsService.read(999L),
                                "Should throw exception when news not found");

                verify(newsRepository).findById(999L);
        }

        @Test
        @DisplayName("Should update news successfully by author")
        void testUpdateSuccessByAuthor() {
                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
                when(newsRepository.save(any(NewsEntity.class))).thenReturn(news);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(any(NewsEntity.class))).thenReturn(previewDTO);
                when(mapper.toNewsDTO(any(NewsEntity.class))).thenReturn(newsDTO);

                NewsInputDTO updateDTO = new NewsInputDTO("Updated Title", "Updated Content", 1L);
                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                newsService.update(1L, updateDTO, authorDetails);

                verify(newsRepository).findById(1L);
                verify(categoryRepository).findById(1L);
                verify(newsRepository).save(any(NewsEntity.class));
                verify(newsWebSocketController).sendNewsUpdate(previewDTO);
        }

        @Test
        @DisplayName("Should throw exception when updating news by non-author non-admin")
        void testUpdateFailsUnauthorized() {

                UserEntity otherUser = new UserEntity("other", "other@example.com", "password", UserRole.USER);
                otherUser.setId(2L);

                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

                NewsInputDTO updateDTO = new NewsInputDTO("Updated Title", "Updated Content", 1L);
                AuthenticatedUserDTO otherDetails = new AuthenticatedUserDTO(otherUser.getId(), otherUser.getLogin(),
                                otherUser.getRole());

                assertThrows(CustomAccessDeniedException.class,
                                () -> newsService.update(1L, updateDTO, otherDetails),
                                "Should throw exception when user is not author and not admin");

                verify(newsRepository).findById(1L);
        }

        @Test
        @DisplayName("Should allow admin to update any news")
        void testUpdateSuccessByAdmin() {

                UserEntity admin = new UserEntity("admin", "admin@example.com", "password", UserRole.ADMIN);
                admin.setId(2L);

                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
                when(newsRepository.save(any(NewsEntity.class))).thenReturn(news);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(any(NewsEntity.class))).thenReturn(previewDTO);
                when(mapper.toNewsDTO(any(NewsEntity.class))).thenReturn(newsDTO);

                NewsInputDTO updateDTO = new NewsInputDTO("Updated Title", "Updated Content", 1L);
                AuthenticatedUserDTO adminDetails = new AuthenticatedUserDTO(admin.getId(), admin.getLogin(),
                                admin.getRole());

                newsService.update(1L, updateDTO, adminDetails);

                verify(newsRepository).findById(1L);
                verify(categoryRepository).findById(1L);
                verify(newsRepository).save(any(NewsEntity.class));
                verify(newsWebSocketController).sendNewsUpdate(previewDTO);
        }

        @Test
        @DisplayName("Should delete news successfully by author")
        void testDeleteSuccessByAuthor() {

                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                doNothing().when(newsRepository).delete(any(NewsEntity.class));
                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                newsService.delete(1L, authorDetails);

                verify(newsRepository).findById(1L);
                verify(newsRepository).delete(any(NewsEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting news by non-author non-admin")
        void testDeleteFailsUnauthorized() {
                UserEntity otherUser = new UserEntity("other", "other@example.com", "password", UserRole.USER);
                otherUser.setId(2L);

                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
                AuthenticatedUserDTO otherDetails = new AuthenticatedUserDTO(otherUser.getId(), otherUser.getLogin(),
                                otherUser.getRole());

                assertThrows(CustomAccessDeniedException.class,
                                () -> newsService.delete(1L, otherDetails),
                                "Should throw exception when user is not author and not admin");

                verify(newsRepository).findById(1L);
                verify(newsRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should allow admin to delete any news")
        void testDeleteSuccessByAdmin() {
                UserEntity admin = new UserEntity("admin", "admin@example.com", "password", UserRole.ADMIN);
                admin.setId(2L);

                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
                doNothing().when(newsRepository).delete(any(NewsEntity.class));
                AuthenticatedUserDTO adminDetails = new AuthenticatedUserDTO(admin.getId(), admin.getLogin(),
                                admin.getRole());

                newsService.delete(1L, adminDetails);

                verify(newsRepository).findById(1L);
                verify(newsRepository).delete(any(NewsEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent news")
        void testDeleteFailsWithInvalidId() {
                when(newsRepository.findById(999L)).thenReturn(Optional.empty());
                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                assertThrows(ResourceNotFoundException.class,
                                () -> newsService.delete(999L, authorDetails),
                                "Should throw exception when news not found");

                verify(newsRepository).findById(999L);
                verify(newsRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should update news category successfully")
        void testUpdateCategoryOnly() {
                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
                when(newsRepository.save(any(NewsEntity.class))).thenReturn(news);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(any(NewsEntity.class))).thenReturn(previewDTO);
                when(mapper.toNewsDTO(any(NewsEntity.class))).thenReturn(newsDTO);

                NewsInputDTO updateDTO = new NewsInputDTO("Test Title", "Test Content", 1L);
                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                NewsDTO result = newsService.update(1L, updateDTO, authorDetails);

                assertNotNull(result);
                verify(newsRepository).findById(1L);
                verify(categoryRepository).findById(1L);
                verify(newsRepository).save(any(NewsEntity.class));
                verify(newsWebSocketController).sendNewsUpdate(previewDTO);
        }

        @Test
        @DisplayName("Should search news by different queries")
        void testSearchMultipleQueries() {
                String query1 = "Java";
                String query2 = "Spring";
                List<NewsEntity> newsList1 = Arrays.asList(news);
                List<NewsEntity> newsList2 = Arrays.asList();
                Page<NewsEntity> pageResult1 = new PageImpl<>(newsList1);
                Page<NewsEntity> pageResult2 = new PageImpl<>(newsList2);

                when(newsRepository.searchByKeyword(eq(query1), any(Pageable.class)))
                                .thenReturn(pageResult1);
                when(newsRepository.searchByKeyword(eq(query2), any(Pageable.class)))
                                .thenReturn(pageResult2);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);

                Page<NewsPreviewDTO> result1 = newsService.search(query1, 0);
                Page<NewsPreviewDTO> result2 = newsService.search(query2, 0);

                assertNotNull(result1);
                assertEquals(1, result1.getTotalElements());
                assertNotNull(result2);
                assertEquals(0, result2.getTotalElements());
                verify(newsRepository, times(2)).searchByKeyword(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter news with empty author list")
        void testFilterWithEmptyAuthors() {
                List<Long> categoryIds = Arrays.asList(1L);
                List<Long> authorIds = Arrays.asList();
                List<NewsEntity> newsList = Arrays.asList(news);
                Page<NewsEntity> pageResult = new PageImpl<>(newsList);

                when(newsRepository.filter(eq(categoryIds), eq(authorIds), any(Pageable.class)))
                                .thenReturn(pageResult);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);

                Page<NewsPreviewDTO> result = newsService.filter(categoryIds, authorIds, 0);

                assertNotNull(result);
                assertEquals(1, result.getTotalElements());
                verify(newsRepository).filter(eq(categoryIds), eq(authorIds), any(Pageable.class));
        }

        @Test
        @DisplayName("Should send WebSocket notification on news creation")
        void testCreateNotifiesWebSocket() {
                when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
                when(newsRepository.save(any(NewsEntity.class))).thenReturn(news);

                UserDTO authorDTO = new UserDTO(1L, "author1", UserRole.AUTHOR, false);
                CategoryDTO categoryDTO = new CategoryDTO(1L, "Technology");
                NewsPreviewDTO previewDTO = new NewsPreviewDTO(1L, "Test Title", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                NewsDTO newsDTO = new NewsDTO(1L, "Test Title", "Test Content", "Test Content", authorDTO, categoryDTO,
                                LocalDateTime.now());
                when(mapper.toNewsPreviewDTO(news)).thenReturn(previewDTO);
                when(mapper.toNewsDTO(news)).thenReturn(newsDTO);

                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                newsService.create(newsInputDTO, authorDetails);

                verify(newsWebSocketController).sendNewsCreate(previewDTO);
        }

        @Test
        @DisplayName("Should send WebSocket notification on news deletion")
        void testDeleteNotifiesWebSocket() {
                when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
                doNothing().when(newsRepository).delete(any(NewsEntity.class));
                AuthenticatedUserDTO authorDetails = new AuthenticatedUserDTO(author.getId(), author.getLogin(),
                                author.getRole());

                newsService.delete(1L, authorDetails);

                verify(newsWebSocketController).sendNewsDelete(1L);
        }
}
