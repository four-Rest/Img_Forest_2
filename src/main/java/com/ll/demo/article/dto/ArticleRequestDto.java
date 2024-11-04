package com.ll.demo.article.dto;

import com.ll.demo.article.entity.Tag;
import com.ll.demo.member.entity.Member;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Validated
public class ArticleRequestDto {
    private String content;
    private String tagString;
    private String[] tagArray;
    private MultipartFile multipartFile;
    private Long price;
    private boolean paid;

    public void parseTags() {
        if (tagString != null && !tagString.isEmpty()) {
            this.tagArray = tagString.split(",");
        }
    }
}
