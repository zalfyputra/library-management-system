package com.library.controller;

import com.library.dto.request.ArticleRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.ArticleResponse;
import com.library.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Articles", description = "Article/Library CRUD operations with RBAC")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @GetMapping
    @Operation(summary = "Get all articles", description = "Retrieve articles based on user role and permissions")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles(Authentication authentication) {
        List<ArticleResponse> articles = articleService.getAllArticles(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID", description = "Retrieve a specific article by ID")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleById(
            @PathVariable Long id, 
            Authentication authentication) {
        ArticleResponse article = articleService.getArticleById(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(article));
    }
    
    @GetMapping("/my-articles")
    @Operation(summary = "Get my articles", description = "Retrieve all articles created by the current user")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getMyArticles(Authentication authentication) {
        List<ArticleResponse> articles = articleService.getMyArticles(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('CONTRIBUTOR', 'EDITOR', 'SUPER_ADMIN')")
    @Operation(summary = "Create article", description = "Create a new article (CONTRIBUTOR, EDITOR, SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @Valid @RequestBody ArticleRequest request,
            Authentication authentication) {
        ArticleResponse article = articleService.createArticle(request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", article));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTRIBUTOR', 'EDITOR', 'SUPER_ADMIN')")
    @Operation(summary = "Update article", description = "Update an article (owner, EDITOR on own, SUPER_ADMIN on any)")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request,
            Authentication authentication) {
        ArticleResponse article = articleService.updateArticle(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", article));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'SUPER_ADMIN')")
    @Operation(summary = "Delete article", description = "Delete an article (EDITOR on own, SUPER_ADMIN on any)")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @PathVariable Long id,
            Authentication authentication) {
        articleService.deleteArticle(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }
}

