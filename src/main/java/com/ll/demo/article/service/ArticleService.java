package com.ll.demo.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo.article.dto.ArticleDetailResponseDto;
import com.ll.demo.article.dto.ArticleRequestDto;
import com.ll.demo.article.dto.ArticleListResponseDto;
import com.ll.demo.article.dto.ArticleRequestDtoMode2;
import com.ll.demo.article.entity.Article;
import com.ll.demo.article.entity.Image;
import com.ll.demo.article.entity.LikeTable;
import com.ll.demo.article.entity.Tag;
import com.ll.demo.article.repository.ArticleRepository;
import com.ll.demo.article.repository.LikeTableRepository;
import com.ll.demo.article.repository.TagRepository;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ImageService imageService;
    private final ArticleTagService articleTagService;
    private final LikeTableRepository likeTableRepository;
    private final TagRepository tagRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void create(ArticleRequestDto articleRequestDto, Member member) throws IOException {

        if(articleRequestDto.getMultipartFile() == null) {
            throw new IllegalArgumentException("적어도 하나의 이미지를 업로드해야 합니다.");
        }
        Article article = Article.builder()
                .content(articleRequestDto.getContent())
                .member(member)
                .price(articleRequestDto.getPrice())
                .paid(articleRequestDto.isPaid())
                .likes(0)
                .build();
        String tagString = articleRequestDto.getTagString();
        if (tagString != null) {
            articleTagService.update(article, tagString);
        }
        Image image = imageService.create(article, articleRequestDto.getMultipartFile());
        article.setImage(image);
        articleRepository.save(article);
    }

    public List<ArticleListResponseDto> findAllOrderByLikesDesc() {
        List<Article> articles = articleRepository.findAllByOrderByLikesDesc();
        return articles.stream()
                .map(ArticleListResponseDto::new)
                .collect(Collectors.toList());
    }

    public Article getArticleById(Long id) {
        Optional<Article> opArticle = articleRepository.findById(id);
        if (opArticle.isPresent()) {
            return opArticle.get();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public void delete(Article article) throws IOException {
        Image image = article.getImage();
        imageService.delete(image);
        articleRepository.delete(article);
    }

    @Transactional
    public void modifyPaidArticle(Article article, ArticleRequestDto articleRequestDto) {
        //내용과 태그 변경
        article.modifyContent(articleRequestDto.getContent());
        if (articleRequestDto.getTagString() != null) {
            articleTagService.update(article, articleRequestDto.getTagString());
        }
    }

    @Transactional
    public void modifyUnpaidArticle(Article article, ArticleRequestDto articleRequestDto) throws IOException {
        //내용과 태그 변경
        article.modifyContent(articleRequestDto.getContent());

        if (articleRequestDto.getTagString() != null) {
            articleTagService.update(article, articleRequestDto.getTagString());
        }
        if (articleRequestDto.getMultipartFile() != null) {
            //이미지 교체
            imageService.modify(article.getImage(), articleRequestDto.getMultipartFile());
        }
    }

    @Transactional
    public void modifyArticle(Article article, ArticleRequestDtoMode2 articleRequestDto) {
        article.modifyContent(articleRequestDto.getContent());
        if (articleRequestDto.getTagString() != null) {
            articleTagService.update(article, articleRequestDto.getTagString());
        }
        articleRepository.save(article);
    }

    public LikeTable getLikeByArticleIdAndMemberId(Long articleId, Long memberId) {
        Optional<LikeTable> opLike = likeTableRepository.getLikeByArticleIdAndMemberId(articleId, memberId);
        if (opLike.isPresent()) {
            return opLike.get();
        }
        return null;
    }

    @Transactional
    public void like(Article article, Member member) {

        if (getLikeByArticleIdAndMemberId(article.getId(), member.getId()) == null) {
            LikeTable likeTable = new LikeTable(article, member);

            likeTableRepository.save(likeTable);
            article.setLikes(article.getLikes() + 1);
        }
    }

    @Transactional
    public void unlike(Article article, Member member) {

        LikeTable likeTable = getLikeByArticleIdAndMemberId(article.getId(), member.getId());

        if (likeTable != null) {
            likeTableRepository.delete(likeTable);
            article.setLikes(article.getLikes() - 1);
        }
    }

    // 게시물 페이징
    public Page<ArticleListResponseDto> searchAllPaging(int pageNo) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("likes"));
        Pageable pageable = PageRequest.of(pageNo,10, Sort.by(sorts));

        return articleRepository.findAll(pageable).map(article -> new ArticleListResponseDto(article));
    }

    // 태그 게시물 페이징
    public Page<ArticleListResponseDto> searchAllPagingByTag(int pageNo, String tagName) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("likes"));
        Pageable pageable = PageRequest.of(pageNo,10, Sort.by(sorts));
        Tag tag = tagRepository.findByTagName(tagName);

        if(tag == null) {
            return articleRepository.findAll(pageable).map(article -> new ArticleListResponseDto(article));
        }

        return articleRepository.findByArticleTagsTag(tag, pageable).map(article -> new ArticleListResponseDto(article));

    }
    // 닉네임 게시물 페이징
    public Page<ArticleListResponseDto> searchAllPagingByUser(int pageNo, String nick) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("likes"));
        Pageable pageable = PageRequest.of(pageNo,10,Sort.by(sorts));
        Member member = memberRepository.findByNickname(nick);

        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + nick);
        }

        return articleRepository.findByMemberNickname(nick,pageable).map(article -> new ArticleListResponseDto(article));
    }

    //최근 읽은 글 저장
    @Transactional
    public void saveRecentReadArticle(Long userId, Long articleId){
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        Optional<Article> opArticle = articleRepository.findById(articleId);
        if (opArticle.isPresent()) {
            ArticleDetailResponseDto articleDetailResponseDto = new ArticleDetailResponseDto(opArticle.get());
            String key = "userId::"+ userId;
            listOperations.leftPush(key, articleDetailResponseDto);
            redisTemplate.expireAt(key, Instant.now().plus(7, ChronoUnit.DAYS)); // 유효기간 TTL 일주일 설정
        }
        else throw new IllegalArgumentException();
    }

    // 최근 읽은 글 조회
    @Transactional
    public ArticleDetailResponseDto findRecentArticle(Long userId, Long articleId) {
        ListOperations<String, Object> listOperations = redisTemplate.opsForList();
        String key = "userId::" + userId;
        Long size = listOperations.size(key);
        List<Object> results = size > 0 ? listOperations.range(key, 0, size) : Collections.emptyList();
        return results.stream()
                .map(this::convertMapToArticleDetailResponseDto)
                .filter(Objects::nonNull) // null이 아닌 객체만 필터링
                .filter(dto -> articleId.equals(dto.getId()))
                .findFirst()
                .orElse(null); // 일치하는 객체가 없으면 null을 반환
    }

    private ArticleDetailResponseDto convertMapToArticleDetailResponseDto(Object o) {
        if (o instanceof Map) {
            // Map 형태의 객체를 ArticleDetailResponseDto 객체로 변환
            return objectMapper.convertValue(o, ArticleDetailResponseDto.class);
        }
        return null; // 변환할 수 없으면 null 반환
    }
    public Article findById(long id) {
        Article article = articleRepository.findById(id).orElseThrow(()->new RuntimeException("존재하지 않는 게시물입니다."));
        return article;
    }

    public Page<ArticleListResponseDto> getLikedPaging(Member member, int pageNo) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(pageNo,10,Sort.by(sorts));

        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다");
        }

        return likeTableRepository.getLikeTableByMemberId(member.getId(), pageable).map(likeTable -> new ArticleListResponseDto(findById(likeTable.getArticleId())));
    }
}