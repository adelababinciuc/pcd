package com.pcd.restservice.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.pcd.restservice.model.Article;
import com.pcd.restservice.model.Link;
import com.pcd.restservice.service.ArticleService;

@Path("/articles")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class ArticleResource {

	ArticleService articleService = new ArticleService();
	
	@GET
	public Response getArticles(@Context UriInfo uriInfo){
		List<Article> articles = new ArrayList<Article>();
		articles = articleService.getAllArticles();
		
		for(Article article : articles){
			String uri = uriInfo.getBaseUriBuilder()
					.path(ArticleResource.class)
					.path(Long.toString(article.getId()))
					.build()
					.toString();
			if(!article.getLinks().contains(new Link(uri, "self"))){
				article.addLink(uri, "self");
			}
		}
		
		GenericEntity<List<Article>> entity = new GenericEntity<List<Article>>(articles) {};
		return Response.status(Status.OK)
				.entity(entity)
				.build();
	}
	
	@GET
	@Path("/{articleId}")
	public Response getArticle(@PathParam("articleId") long articleId, @Context UriInfo uriInfo){
		Article article = articleService.getArticle(articleId);
		
		String uri = uriInfo.getBaseUriBuilder()
				.path(ArticleResource.class)
				.path(Long.toString(article.getId()))
				.build()
				.toString();
		if(!article.getLinks().contains(new Link(uri, "self"))){
			article.addLink(uri, "self");
		}
		
		String uri1 = uriInfo.getBaseUriBuilder()
				.path(ArticleResource.class)
				.build()
				.toString();
		if(!article.getLinks().contains(new Link(uri1, "articles"))){
			article.addLink(uri1, "articles");
		}
				
		GenericEntity<Article> entity = new GenericEntity<Article>(article) {};
		return Response.status(Status.OK)
				.entity(entity)
				.build();
	}
	
	@POST
	public Response addArticle(Article article,  @Context UriInfo uriInfo){
		Article addedArticle = articleService.addArticle(article);
		String uri = uriInfo.getBaseUriBuilder()
				.path(ArticleResource.class)
				.build()
				.toString();
		if(!article.getLinks().contains(new Link(uri, "self"))){
			article.addLink(uri, "self");
		}
		return Response.status(Status.CREATED)
				.entity(addedArticle)
				.build();
	}
	
	@PUT
	@Path("/{articleId}")
	public Response addArticle(@PathParam("articleId") long articleId, Article article,  @Context UriInfo uriInfo){
		Article addedArticle = articleService.addArticle(articleId, article);
		String uri = uriInfo.getBaseUriBuilder()
				.path(ArticleResource.class)
				.path(Long.toString(addedArticle.getId()))
				.build()
				.toString();
		if(!article.getLinks().contains(new Link(uri, "self"))){
			article.addLink(uri, "self");
		}
		
		String uri1 = uriInfo.getBaseUriBuilder()
				.path(ArticleResource.class)
				.build()
				.toString();
		if(!article.getLinks().contains(new Link(uri1, "articles"))){
			article.addLink(uri1, "articles");
		}
		
		return Response.status(Status.CREATED)
				.entity(articleService.addArticle(articleId, article))
				.build();
	}
	
	@DELETE
	@Path("/{articleId}")
	public Response deleteArticle(@PathParam("articleId") long articleId){
		articleService.removeArticle(articleId);
		return Response.status(Status.NO_CONTENT)
				.build();
	}
}
