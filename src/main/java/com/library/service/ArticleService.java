package com.library.service;

import com.library.domain.entity.Article;
import com.library.domain.entity.User;
import com.library.domain.enums.AuditAction;
import com.library.domain.enums.Role;
import com.library.dto.request.ArticleRequest;
import com.library.dto.response.ArticleResponse;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedException;
import com.library.repository.ArticleRepository;
import com.library.util.BubbleSortUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private BubbleSortUtil bubbleSortUtil;
    
    @Cacheable(value = "articles", key = "'all'")
    public List<ArticleResponse> getAllArticles(String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        List<Article> articles;
        
        // SUPER_ADMIN can see all articles
        if (currentUser.getRole() == Role.SUPER_ADMIN || currentUser.getRole() == Role.EDITOR) {
            articles = articleRepository.findAll();
        } 
        // VIEWER can only see public articles
        else if (currentUser.getRole() == Role.VIEWER) {
            articles = articleRepository.findAllPublicArticles();
        }
        // CONTRIBUTOR can see their own and public articles
        else {
            articles = articleRepository.findByAuthorIdOrPublic(currentUser.getId());
        }
        
        // Sort articles by created date using bubble sort
        bubbleSortUtil.sortArticlesByCreatedDate(articles);
        
        return articles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "articles", key = "#id")
    public ArticleResponse getArticleById(Long id, String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        Article article = articleRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        
        // Check if user has permission to view this article
        if (!canViewArticle(article, currentUser)) {
            throw new UnauthorizedException("You don't have permission to view this article");
        }
        
        // Log article view
        auditLogService.logArticleAction(AuditAction.ARTICLE_VIEWED, 
            currentUser.getId(), currentUser.getUsername(), article.getId(),
            "Viewed article: " + article.getTitle());
        
        return convertToResponse(article);
    }
    
    public List<ArticleResponse> getMyArticles(String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        List<Article> articles = articleRepository.findByAuthorId(currentUser.getId());
        
        // Sort by updated date using bubble sort
        bubbleSortUtil.sortArticlesByUpdatedDate(articles);
        
        return articles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleResponse createArticle(ArticleRequest request, String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        
        // Check if user has permission to create articles
        if (currentUser.getRole() == Role.VIEWER) {
            throw new UnauthorizedException("Viewers cannot create articles");
        }
        
        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(currentUser.getId())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .build();
        
        article = articleRepository.save(article);
        
        // Log article creation
        auditLogService.logArticleAction(AuditAction.ARTICLE_CREATED, 
            currentUser.getId(), currentUser.getUsername(), article.getId(),
            "Created article: " + article.getTitle());
        
        return convertToResponse(article);
    }
    
    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public ArticleResponse updateArticle(Long id, ArticleRequest request, String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        
        // Check if user has permission to update this article
        if (!canUpdateArticle(article, currentUser)) {
            throw new UnauthorizedException("You don't have permission to update this article");
        }
        
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        if (request.getIsPublic() != null) {
            article.setIsPublic(request.getIsPublic());
        }
        
        article = articleRepository.save(article);
        
        // Log article update
        auditLogService.logArticleAction(AuditAction.ARTICLE_UPDATED, 
            currentUser.getId(), currentUser.getUsername(), article.getId(),
            "Updated article: " + article.getTitle());
        
        return convertToResponse(article);
    }
    
    @Transactional
    @CacheEvict(value = "articles", allEntries = true)
    public void deleteArticle(Long id, String currentUsername) {
        User currentUser = userService.getUserEntityByUsername(currentUsername);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        
        // Check if user has permission to delete this article
        if (!canDeleteArticle(article, currentUser)) {
            throw new UnauthorizedException("You don't have permission to delete this article");
        }
        
        String articleTitle = article.getTitle();
        articleRepository.delete(article);
        
        // Log article deletion
        auditLogService.logArticleAction(AuditAction.ARTICLE_DELETED, 
            currentUser.getId(), currentUser.getUsername(), id,
            "Deleted article: " + articleTitle);
    }
    
    private boolean canViewArticle(Article article, User user) {
        // SUPER_ADMIN and EDITOR can view all
        if (user.getRole() == Role.SUPER_ADMIN || user.getRole() == Role.EDITOR) {
            return true;
        }
        
        // Owner can view their own articles
        if (article.getAuthorId().equals(user.getId())) {
            return true;
        }
        
        // Everyone can view public articles
        return article.getIsPublic();
    }
    
    private boolean canUpdateArticle(Article article, User user) {
        // SUPER_ADMIN can update any article
        if (user.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        
        // EDITOR and CONTRIBUTOR can update their own articles
        if ((user.getRole() == Role.EDITOR || user.getRole() == Role.CONTRIBUTOR) 
            && article.getAuthorId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }
    
    private boolean canDeleteArticle(Article article, User user) {
        // SUPER_ADMIN can delete any article
        if (user.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        
        // EDITOR can delete their own articles
        if (user.getRole() == Role.EDITOR && article.getAuthorId().equals(user.getId())) {
            return true;
        }
        
        // CONTRIBUTOR cannot delete articles
        return false;
    }
    
    private ArticleResponse convertToResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .authorId(article.getAuthorId())
                .authorName(article.getAuthor() != null ? article.getAuthor().getFullname() : null)
                .isPublic(article.getIsPublic())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}

