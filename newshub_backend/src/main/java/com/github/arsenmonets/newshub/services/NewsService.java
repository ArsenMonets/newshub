package com.github.arsenmonets.newshub.services;

import com.github.arsenmonets.newshub.dto.AuthenticatedUserDTO;
import com.github.arsenmonets.newshub.dto.NewsInputDTO;
import com.github.arsenmonets.newshub.dto.NewsPreviewDTO;
import com.github.arsenmonets.newshub.dto.NewsDTO;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsService {

        private final NewsRepository newsRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;
        private final NewsWebSocketController newsWebSocketController;
        private final NewsHubMapper mapper;

        public NewsService(NewsRepository newsRepository, CategoryRepository categoryRepository,
                        UserRepository userRepository, NewsWebSocketController newsWebSocketController,
                        NewsHubMapper mapper) {
                this.newsRepository = newsRepository;
                this.categoryRepository = categoryRepository;
                this.userRepository = userRepository;
                this.newsWebSocketController = newsWebSocketController;
                this.mapper = mapper;
        }

        public Page<NewsPreviewDTO> search(String query, int page) {
                return newsRepository
                                .searchByKeyword(query, PageRequest.of(page, 10, Sort.by("createdAt").descending()))
                                .map(mapper::toNewsPreviewDTO);
        }

        public Page<NewsPreviewDTO> filter(List<Long> categoryIds, List<Long> authorIds, int page) {
                return newsRepository
                                .filter(categoryIds, authorIds,
                                                PageRequest.of(page, 10, Sort.by("createdAt").descending()))
                                .map(mapper::toNewsPreviewDTO);
        }

        @Transactional
        public NewsDTO create(NewsInputDTO data, AuthenticatedUserDTO currentUser) {
                CategoryEntity category = categoryRepository.findById(data.categoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Категорію не знайдено"));

                UserEntity author = userRepository.getReferenceById(currentUser.id());
                NewsEntity news = new NewsEntity(data.title(), data.content(), author, category);
                NewsEntity savedNews = newsRepository.save(news);
                newsWebSocketController.sendNewsCreate(mapper.toNewsPreviewDTO(savedNews));
                return mapper.toNewsDTO(savedNews);
        }

        @Transactional
        public NewsDTO update(Long id, NewsInputDTO data, AuthenticatedUserDTO currentUser) {
                NewsEntity news = newsRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Новину не знайдено"));
                if (!news.getAuthor().getId().equals(currentUser.id()) && !(currentUser.role() == UserRole.ADMIN)) {
                        throw new CustomAccessDeniedException("У вас немає прав для оновлення цієї новини");
                }

                CategoryEntity category = categoryRepository.findById(data.categoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Категорію не знайдено"));

                news.setTitle(data.title());
                news.setContent(data.content());
                news.setCategory(category);

                NewsEntity updatedNews = newsRepository.save(news);
                newsWebSocketController.sendNewsUpdate(mapper.toNewsPreviewDTO(updatedNews));
                return mapper.toNewsDTO(updatedNews);
        }

        @Transactional
        public void delete(Long id, AuthenticatedUserDTO currentUser) {
                NewsEntity news = newsRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Новину не знайдено"));
                if (!news.getAuthor().getId().equals(currentUser.id()) && !(currentUser.role() == UserRole.ADMIN)) {
                        throw new CustomAccessDeniedException("У вас немає прав для видалення цієї новини");
                }

                newsRepository.delete(news);
                newsWebSocketController.sendNewsDelete(id);
        }

        public NewsDTO read(Long id) {
                return newsRepository.findById(id)
                                .map(mapper::toNewsDTO)
                                .orElseThrow(() -> new ResourceNotFoundException("Новину не знайдено із ID: " + id));
        }
}
