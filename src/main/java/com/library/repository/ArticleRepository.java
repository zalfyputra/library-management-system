package com.library.repository;

import com.library.domain.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    List<Article> findByAuthorId(Long authorId);
    
    List<Article> findByIsPublic(Boolean isPublic);
    
    @Query("SELECT a FROM Article a WHERE a.isPublic = true")
    List<Article> findAllPublicArticles();
    
    @Query("SELECT a FROM Article a WHERE a.authorId = :authorId OR a.isPublic = true")
    List<Article> findByAuthorIdOrPublic(Long authorId);
    
    Optional<Article> findByIdAndAuthorId(Long id, Long authorId);
    
    @Query("SELECT a FROM Article a JOIN FETCH a.author WHERE a.id = :id")
    Optional<Article> findByIdWithAuthor(Long id);
}

