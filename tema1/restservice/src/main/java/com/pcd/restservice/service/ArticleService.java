package com.pcd.restservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pcd.restservice.database.DatabaseClass;
import com.pcd.restservice.model.Article;

public class ArticleService {
	
	private Map<Long, Article> articles = DatabaseClass.getArticles();

	public ArticleService() {
		articles.put(0L, new Article(0, "title0", "content0"));
		articles.put(1L, new Article(1, "title1", "content1"));
	}

	public List<Article> getAllArticles(){		
		return new ArrayList<>(articles.values());
	}
	
	public Article getArticle(long id){
		return articles.get(id);
	}
	
	public Article addArticle(Article article){
		article.setId(articles.size());
		articles.put(article.getId(), article);
		return article;
	}
	
	public Article addArticle(long id, Article article){
		articles.put(id, article);
		return article;
	}
	
	public Article removeArticle(long id){
		return articles.remove(id);
	}
}
