package com.library.util;

import com.library.domain.entity.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BubbleSortUtilTest {
    
    private BubbleSortUtil bubbleSortUtil;
    
    @BeforeEach
    void setUp() {
        bubbleSortUtil = new BubbleSortUtil();
    }
    
    @Test
    void testBubbleSortWithComparableList() {
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);
        bubbleSortUtil.bubbleSort(numbers);
        
        assertEquals(Arrays.asList(1, 2, 5, 8, 9), numbers);
    }
    
    @Test
    void testBubbleSortWithEmptyList() {
        List<Integer> numbers = new ArrayList<>();
        bubbleSortUtil.bubbleSort(numbers);
        
        assertTrue(numbers.isEmpty());
    }
    
    @Test
    void testBubbleSortWithNullList() {
        assertDoesNotThrow(() -> bubbleSortUtil.bubbleSort((List<Integer>) null));
    }
    
    @Test
    void testBubbleSortWithSingleElement() {
        List<Integer> numbers = Arrays.asList(5);
        bubbleSortUtil.bubbleSort(numbers);
        
        assertEquals(Arrays.asList(5), numbers);
    }
    
    @Test
    void testBubbleSortArray() {
        int[] array = {5, 2, 8, 1, 9};
        bubbleSortUtil.bubbleSortArray(array);
        
        assertArrayEquals(new int[]{1, 2, 5, 8, 9}, array);
    }
    
    @Test
    void testBubbleSortArrayAlreadySorted() {
        int[] array = {1, 2, 3, 4, 5};
        bubbleSortUtil.bubbleSortArray(array);
        
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, array);
    }
    
    @Test
    void testSortArticlesByTitle() {
        List<Article> articles = new ArrayList<>();
        
        Article article1 = Article.builder().id(1L).title("Zebra").build();
        Article article2 = Article.builder().id(2L).title("Apple").build();
        Article article3 = Article.builder().id(3L).title("Mango").build();
        
        articles.add(article1);
        articles.add(article2);
        articles.add(article3);
        
        bubbleSortUtil.sortArticlesByTitle(articles);
        
        assertEquals("Apple", articles.get(0).getTitle());
        assertEquals("Mango", articles.get(1).getTitle());
        assertEquals("Zebra", articles.get(2).getTitle());
    }
    
    @Test
    void testSortArticlesByCreatedDate() {
        List<Article> articles = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        Article article1 = Article.builder().id(1L).title("Old").createdAt(now.minusDays(5)).build();
        Article article2 = Article.builder().id(2L).title("New").createdAt(now).build();
        Article article3 = Article.builder().id(3L).title("Middle").createdAt(now.minusDays(2)).build();
        
        articles.add(article1);
        articles.add(article2);
        articles.add(article3);
        
        bubbleSortUtil.sortArticlesByCreatedDate(articles);
        
        // Should be sorted newest first
        assertEquals("New", articles.get(0).getTitle());
        assertEquals("Middle", articles.get(1).getTitle());
        assertEquals("Old", articles.get(2).getTitle());
    }
}

