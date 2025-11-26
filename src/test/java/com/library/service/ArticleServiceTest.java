package com.library.service;

import com.library.domain.entity.Article;
import com.library.domain.entity.User;
import com.library.domain.enums.Role;
import com.library.dto.request.ArticleRequest;
import com.library.dto.response.ArticleResponse;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedException;
import com.library.repository.ArticleRepository;
import com.library.util.BubbleSortUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleServiceTest {
    
    @Mock
    private ArticleRepository articleRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private BubbleSortUtil bubbleSortUtil;
    
    @InjectMocks
    private ArticleService articleService;
    
    private User viewerUser;
    private User contributorUser;
    private User editorUser;
    private User superAdminUser;
    private Article article;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        viewerUser = User.builder().id(1L).username("viewer").role(Role.VIEWER).build();
        contributorUser = User.builder().id(2L).username("contributor").role(Role.CONTRIBUTOR).build();
        editorUser = User.builder().id(3L).username("editor").role(Role.EDITOR).build();
        superAdminUser = User.builder().id(4L).username("admin").role(Role.SUPER_ADMIN).build();
        
        article = Article.builder()
                .id(1L)
                .title("Test Article")
                .content("Test Content")
                .authorId(2L)
                .isPublic(true)
                .build();
    }
    
    @Test
    void testGetAllArticlesAsViewer() {
        when(userService.getUserEntityByUsername("viewer")).thenReturn(viewerUser);
        when(articleRepository.findAllPublicArticles()).thenReturn(Arrays.asList(article));
        
        List<ArticleResponse> articles = articleService.getAllArticles("viewer");
        
        assertNotNull(articles);
        assertEquals(1, articles.size());
        verify(bubbleSortUtil, times(1)).sortArticlesByCreatedDate(any());
    }
    
    @Test
    void testGetAllArticlesAsSuperAdmin() {
        when(userService.getUserEntityByUsername("admin")).thenReturn(superAdminUser);
        when(articleRepository.findAll()).thenReturn(Arrays.asList(article));
        
        List<ArticleResponse> articles = articleService.getAllArticles("admin");
        
        assertNotNull(articles);
        assertEquals(1, articles.size());
    }
    
    @Test
    void testGetArticleById() {
        when(userService.getUserEntityByUsername("viewer")).thenReturn(viewerUser);
        when(articleRepository.findByIdWithAuthor(1L)).thenReturn(Optional.of(article));
        
        ArticleResponse response = articleService.getArticleById(1L, "viewer");
        
        assertNotNull(response);
        assertEquals("Test Article", response.getTitle());
        verify(auditLogService, times(1)).logArticleAction(any(), anyLong(), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testGetArticleByIdNotFound() {
        when(userService.getUserEntityByUsername("viewer")).thenReturn(viewerUser);
        when(articleRepository.findByIdWithAuthor(999L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, 
            () -> articleService.getArticleById(999L, "viewer"));
    }
    
    @Test
    void testCreateArticleAsContributor() {
        ArticleRequest request = ArticleRequest.builder()
                .title("New Article")
                .content("New Content")
                .isPublic(true)
                .build();
        
        when(userService.getUserEntityByUsername("contributor")).thenReturn(contributorUser);
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        
        ArticleResponse response = articleService.createArticle(request, "contributor");
        
        assertNotNull(response);
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(auditLogService, times(1)).logArticleAction(any(), anyLong(), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testCreateArticleAsViewer() {
        ArticleRequest request = ArticleRequest.builder()
                .title("New Article")
                .content("New Content")
                .build();
        
        when(userService.getUserEntityByUsername("viewer")).thenReturn(viewerUser);
        
        assertThrows(UnauthorizedException.class, 
            () -> articleService.createArticle(request, "viewer"));
    }
    
    @Test
    void testUpdateArticleAsOwner() {
        ArticleRequest request = ArticleRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();
        
        when(userService.getUserEntityByUsername("contributor")).thenReturn(contributorUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        
        ArticleResponse response = articleService.updateArticle(1L, request, "contributor");
        
        assertNotNull(response);
        verify(articleRepository, times(1)).save(any(Article.class));
    }
    
    @Test
    void testUpdateArticleUnauthorized() {
        ArticleRequest request = ArticleRequest.builder()
                .title("Updated Title")
                .content("Updated Content")
                .build();
        
        Article otherUserArticle = Article.builder()
                .id(2L)
                .title("Other Article")
                .authorId(999L)
                .build();
        
        when(userService.getUserEntityByUsername("contributor")).thenReturn(contributorUser);
        when(articleRepository.findById(2L)).thenReturn(Optional.of(otherUserArticle));
        
        assertThrows(UnauthorizedException.class, 
            () -> articleService.updateArticle(2L, request, "contributor"));
    }
    
    @Test
    void testDeleteArticleAsEditor() {
        when(userService.getUserEntityByUsername("editor")).thenReturn(editorUser);
        
        Article editorArticle = Article.builder()
                .id(1L)
                .title("Editor Article")
                .authorId(3L)
                .build();
        
        when(articleRepository.findById(1L)).thenReturn(Optional.of(editorArticle));
        
        articleService.deleteArticle(1L, "editor");
        
        verify(articleRepository, times(1)).delete(any(Article.class));
        verify(auditLogService, times(1)).logArticleAction(any(), anyLong(), anyString(), anyLong(), anyString());
    }
    
    @Test
    void testDeleteArticleAsContributorUnauthorized() {
        when(userService.getUserEntityByUsername("contributor")).thenReturn(contributorUser);
        
        Article contributorArticle = Article.builder()
                .id(1L)
                .authorId(2L)
                .build();
        
        when(articleRepository.findById(1L)).thenReturn(Optional.of(contributorArticle));
        
        assertThrows(UnauthorizedException.class, 
            () -> articleService.deleteArticle(1L, "contributor"));
    }
    
    @Test
    void testGetMyArticles() {
        when(userService.getUserEntityByUsername("contributor")).thenReturn(contributorUser);
        when(articleRepository.findByAuthorId(2L)).thenReturn(Arrays.asList(article));
        
        List<ArticleResponse> articles = articleService.getMyArticles("contributor");
        
        assertNotNull(articles);
        assertEquals(1, articles.size());
        verify(bubbleSortUtil, times(1)).sortArticlesByUpdatedDate(any());
    }
}
