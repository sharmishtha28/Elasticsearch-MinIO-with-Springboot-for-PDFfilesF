package com.spring.pdfjson.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName="pdfjson")
public class Content {

	@Id
	public String id;
	
	@Field(type = FieldType.Keyword, name = "userId")
	public String userId;
	
	@Field(type = FieldType.Keyword, name = "filename")
	public String filename;
	
	@Field(type = FieldType.Keyword, name = "data")
	public String data;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Content(String id, String data, String userId, String filename) {
		super();
		this.id = id;
		this.data = data;
		this.userId = userId;
		this.filename = filename;
	}

	public Content() {
		super();
	}

}


