package com.ll.demo.article.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleRequestDtoMode2 {
    private String content;
    private String[] tagArray;
    private String tagString;

    public void parseTags() {
        if (tagString != null && !tagString.isEmpty()) {
            this.tagArray = tagString.split(",");
        }
    }
}
