package com.ll.demo.article.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.ArticleTag;
import com.ll.demo.article.entity.Tag;
import com.ll.demo.article.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleTagService {

    private final ArticleTagRepository articleTagRepository;
    private final TagService tagService;

    @Transactional
    public void update(Article article, String[] tagArray) {
        Set<Tag> newTags = tagService.toTagSet(tagArray);
        //이미 tag가 있는 경우
        if (article.getArticleTags() != null) {
            Set<Tag> oldTags = article.getArticleTags()
                    .stream()
                    .map(articleTag -> articleTag.getTag())
                    .collect(Collectors.toSet());

            for (Tag oldTag : oldTags) {
                //기존 태그가 새 태그 set에 없으면 삭제
                if (!newTags.contains(oldTag)) {
                    ArticleTag articleTag = findByArticleAndTag(article, oldTag);
                    delete(articleTag);
                }
            }

            for (Tag newTag : newTags) {
                //새 태그가 기존 태그 set에 없으면 추가
                if (!oldTags.contains(newTag)) {
                    ArticleTag articleTag = ArticleTag.builder()
                            .article(article)
                            .tag(newTag)
                            .build();
                    articleTagRepository.save(articleTag);
                }
            }
        }
    }

    public ArticleTag findByArticleAndTag(Article article, Tag tag) {
        ArticleTag articleTag = articleTagRepository.findByArticleAndTag(article, tag);
        return articleTag;
    }

    @Transactional
    public void delete(ArticleTag articleTag) {
        articleTagRepository.delete(articleTag);
    }
}