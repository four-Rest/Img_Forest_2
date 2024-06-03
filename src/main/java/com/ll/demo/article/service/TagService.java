package com.ll.demo.article.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ll.demo.article.entity.Tag;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public Tag create(String tagName) {
            Tag tag = new Tag();
            tag.setTagName(tagName.toLowerCase());
            tagRepository.save(tag);
            return tag;
    }


    public Set<Tag> toTagSet(String[] tagArray) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagArray) {
            if (tagRepository.findByTagName(tagName) != null) {
                tags.add(tagRepository.findByTagName(tagName));
            } else {
                tags.add(create(tagName));
            }
        }
        return tags;
    }

    public Set<Article> getArticlesByTagName(String tagName) {
        Tag tag = tagRepository.findByTagName(tagName);
        if (tag == null) {
            return null;
        }
        Set<Article> articles = tag.getArticleTags()
                .stream()
                .map(articleTag -> articleTag.getArticle())
                .collect(Collectors.toSet());
        return articles;
    }
}
