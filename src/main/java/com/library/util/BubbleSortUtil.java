package com.library.util;

import com.library.domain.entity.Article;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class BubbleSortUtil {
    
    public <T extends Comparable<T>> void bubbleSort(List<T> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        
        int n = list.size();
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).compareTo(list.get(j + 1)) > 0) {
                    // Swap elements
                    T temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swapped = true;
                }
            }
            
            if (!swapped) {
                break;
            }
        }
    }
    
    public <T> void bubbleSort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) {
            return;
        }
        
        int n = list.size();
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            
            for (int j = 0; j < n - i - 1; j++) {
                if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                    // Swap elements
                    T temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swapped = true;
                }
            }
            
            if (!swapped) {
                break;
            }
        }
    }
    
    /**
     * Sort articles by title (ascending)
     */
    public void sortArticlesByTitle(List<Article> articles) {
        bubbleSort(articles, Comparator.comparing(Article::getTitle));
    }
    
    /**
     * Sort articles by creation date (newest first)
     */
    public void sortArticlesByCreatedDate(List<Article> articles) {
        bubbleSort(articles, Comparator.comparing(Article::getCreatedAt).reversed());
    }
    
    /**
     * Sort articles by update date (newest first)
     */
    public void sortArticlesByUpdatedDate(List<Article> articles) {
        bubbleSort(articles, Comparator.comparing(Article::getUpdatedAt).reversed());
    }
    
    /**
     * Sort articles by author ID
     */
    public void sortArticlesByAuthor(List<Article> articles) {
        bubbleSort(articles, Comparator.comparing(Article::getAuthorId));
    }
    
    public void bubbleSortArray(int[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        
        int n = array.length;
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    // Swap elements
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;
                }
            }
            
            if (!swapped) {
                break;
            }
        }
    }
}
