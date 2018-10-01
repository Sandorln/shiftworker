package com.kinggo.app.shiftworker.view.community.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author sangsik.kim
 */

public class Post implements Serializable {
    private long id;
    private String title;
    private String author;
    private String content;
    private String createdDate;
    private String viewCount;
    private String commentsCount;
    private List<Comment> comments;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getViewCount() {
        return viewCount;
    }

    public String getCommentsCount() {
        return commentsCount;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}