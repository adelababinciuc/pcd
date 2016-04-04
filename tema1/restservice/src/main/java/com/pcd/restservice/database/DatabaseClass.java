package com.pcd.restservice.database;

import java.util.HashMap;
import java.util.Map;

import com.pcd.restservice.model.Article;

public class DatabaseClass {

	private static Map<Long, Article> articles = new HashMap<>();

	public static Map<Long, Article> getArticles() {
		return articles;
	}

}
