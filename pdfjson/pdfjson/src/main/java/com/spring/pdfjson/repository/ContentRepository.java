package com.spring.pdfjson.repository;

import java.util.List;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.spring.pdfjson.model.Content;

public interface ContentRepository extends ElasticsearchRepository<Content, String>{
	
	@Query("{\"bool\": { " +
	        "   \"filter\": [ " +
	        "    {  " +
	        "       \"term\": { " +
	        "          \"userId.keyword\": \"?1\" " +
	        "        } " +
	        "    }, " +
	        "    {" +
	        "       \"match\": { " +
	        "          \"data\": \"?0\" " +
	        "       }" +
	        "     } " +
	        "   ] " +
	        "  }}") 	
	List<Content> searchInBody(String keyword,String userId);

	List<Content> findByUserId(String userId);      
}


