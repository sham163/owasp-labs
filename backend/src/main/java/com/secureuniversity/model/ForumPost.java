package com.secureuniversity.model;

import java.time.LocalDateTime;

public class ForumPost {
    private Long id;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;

    public ForumPost() {}
    public ForumPost(Long id, Long authorId, String content, LocalDateTime createdAt) {
        this.id=id; this.authorId=authorId; this.content=content; this.createdAt=createdAt;
    }
    public Long getId(){ return id; }
    public Long getAuthorId(){ return authorId; }
    public String getContent(){ return content; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setId(Long id){ this.id=id; }
    public void setAuthorId(Long authorId){ this.authorId=authorId; }
    public void setContent(String content){ this.content=content; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt=createdAt; }
}
