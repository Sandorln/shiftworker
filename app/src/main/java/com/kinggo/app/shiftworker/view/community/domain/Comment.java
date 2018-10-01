package com.kinggo.app.shiftworker.view.community.domain;

import java.io.Serializable;

/**
 * @author sangsik.kim
 */
public class Comment implements Serializable {
    private long id;
    private long postId;
    private String content;
    private String author;
    private String createdDate;

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public long getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreatedDate() {
        return createdDate;
    }
}
