package com.example.appfirebase.model;

public class Post {
    private String title;
    private String id;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post(String title, String id, String content) {
        this.title = title;
        this.id = id;
        this.content = content;
    }

    public Post() {

    }
}
