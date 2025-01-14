package com.ll.demo.article.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ll.demo.article.dto.*;
import com.ll.demo.article.entity.Article;
import com.ll.demo.article.service.ArticleService;
import com.ll.demo.article.service.ImageService;
import com.ll.demo.article.service.TagService;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.global.rq.Rq;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
@Tag(name = "Article", description = "Article API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "요청에 실패했습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "요청이 거부되었습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "리소스를 서버에서 찾을 수 없습니다.", content = @Content(mediaType = "application/json"))
})
public class ArticleController {

    private final ArticleService articleService;
    private final MemberService memberService;
    private final TagService tagService;
    private final ImageService imageService;
    private final Rq rq;

//    //전체 글 조회
//    @GetMapping("")
//    @Operation(summary = "전체 글 조회", description = "전체 글 조회 시 사용하는 API")
//    public GlobalResponse findAllArticles() {
//        List<ArticleListResponseDto> articleListResponseDtoList = articleService.findAllOrderByLikesDesc();
//        return GlobalResponse.of("200", "success", articleListResponseDtoList);
//    }

    //단일 글 조회
    @GetMapping("/detail/{id}")
    @Operation(summary = "단일 글 조회", description = "단일 글 조회 시 사용하는 API")
    public GlobalResponse showArticle(@PathVariable("id") Long id)  {
        Long memberId = null;
        if(rq.isLoggedIn()){
            memberId = memberService.findByUsername(rq.getUser().getUsername()).getId();
            // 로그인 했고, Redis에서 글을 찾아보기
            ArticleDetailResponseDto articleDetailResponseDto = articleService.findRecentArticle(memberId, id);
            if(articleDetailResponseDto != null) {
                // Redis에서 찾은 글이 있으면, 그 글로 ArticleDetailResponseDto 생성 후 반환
                articleDetailResponseDto.setLikeValue(
                        articleService.getLikeByArticleIdAndMemberId(articleDetailResponseDto.getId(), memberId) != null
                );
                return GlobalResponse.of("200", "success", articleDetailResponseDto);
            }
            else{
                // Redis에서 찾지 못했으면, DB에서 글을 조회하고 Redis에 저장
                articleService.saveRecentReadArticle(memberId, id);
            }
        }
        // DB에서 글 조회 로직
        Article article = articleService.getArticleById(id);
        ArticleDetailResponseDto articleDetailResponseDto = new ArticleDetailResponseDto(article);

        // 좋아요 상태 설정
        boolean isLiked = rq.isLoggedIn() && articleService.getLikeByArticleIdAndMemberId(article.getId(), memberId) != null;
        articleDetailResponseDto.setLikeValue(isLiked);

        return GlobalResponse.of("200", "success", articleDetailResponseDto);
    }

    //tag값으로 검색
    @GetMapping("/{tagName}")
    @Operation(summary = "Tag값으로 글 검색", description = "Tag값으로 글 검색 시 사용하는 API")
    public GlobalResponse searchArticlesByTag(@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                                              @RequestParam(value = "tagName", required = false) String tagName) {
        Page<ArticleListResponseDto> result;

        if(tagName == null) {
            result = articleService.searchAllPaging(pageNo);
        }
        else {
            result = articleService.searchAllPagingByTag(pageNo,tagName);
        }
        return GlobalResponse.of("200","success", result);
    }

    // 글 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    @Operation(summary = "글 생성", description = "글 생성 시 사용하는 API")
    public GlobalResponse createArticle(
            @Valid ArticleRequestDto articleRequestDto,
            Principal principal) throws IOException {
        // 사용자 확인
        Member member = memberService.findByUsername(principal.getName());

        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        }

        Article article = articleService.create(articleRequestDto, member);

        return GlobalResponse.of("200", "Article created", article.getId());
    }


    //글 수정
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @Operation(summary = "글 수정", description = "글 수정 시 사용하는 API")
    public GlobalResponse updateArticle(
            @PathVariable("id") Long id,
            Principal principal,
            @Valid ArticleRequestDto articleRequestDto
    ) throws IOException {
        Article article = articleService.getArticleById(id);
        Member member = memberService.findByUsername(principal.getName());

        //권한 확인
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        } else if (article.getMember().getId() != member.getId()) {
            return GlobalResponse.of("403", "수정 권한이 없습니다.");
        }

        //유료 아티클의 경우 이미지 수정이 불가능함
        if (article.isPaid()) {
            articleService.modifyPaidArticle(article, articleRequestDto);
        }
        //무료 아티클의 경우 이미지 수정이 가능함
        else {
            articleService.modifyUnpaidArticle(article, articleRequestDto);
        }
        return GlobalResponse.of("200", "수정되었습니다.");
    }

    //이미지 없는 수정
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/mode2/{id}")
    @Operation(summary = "이미지 없는 글 수정", description = "이미지 없이 글 수정 시 사용하는 API")
    public GlobalResponse updateArticle2(
            @PathVariable("id") Long id,
            Principal principal,
            @RequestBody ArticleRequestDtoMode2 articleRequestDto
    )  {
        Article article = articleService.getArticleById(id);
        Member member = memberService.findByUsername(principal.getName());

        //권한 확인
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        } else if (article.getMember().getId() != member.getId()) {
            return GlobalResponse.of("403", "수정 권한이 없습니다.");
        }

        articleService.modifyArticle(article, articleRequestDto);

        return GlobalResponse.of("200", "수정되었습니다.");
    }


    //글 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    @Operation(summary = "글 삭제", description = "글 삭제 시 사용하는 API")
    public GlobalResponse deleteArticle(@PathVariable("id") Long id, Principal principal) throws IOException {

        Article article = articleService.getArticleById(id);
        Member member = memberService.findByUsername(principal.getName());

        //권한 확인
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        } else if (article.getMember().getId() != member.getId()) {
            return GlobalResponse.of("403", "삭제 권한이 없습니다.");
        }

        articleService.delete(article);
        return GlobalResponse.of("200", "Article deleted");
    }

    //글 추천
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like/{id}")
    @Operation(summary = "like를 이용한 글 추천 ", description = "글 추천 시 사용하는 API")
    public GlobalResponse likeArticle(@PathVariable("id") Long id, Principal principal) {

        Article article = articleService.getArticleById(id);
        Member member = memberService.findByUsername(principal.getName());

        articleService.like(article, member);

        return GlobalResponse.of("200", "추천되었습니다.");
    }

    //추천 취소
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/like/{id}")
    @Operation(summary = "like를 이용한 글 추천취소", description = "글 추천취소 시 사용하는 API")
    public GlobalResponse unlikeArticle(@PathVariable("id") Long id, Principal principal) {

        Article article = articleService.getArticleById(id);
        Member member = memberService.findByUsername(principal.getName());

        articleService.unlike(article, member);

        return GlobalResponse.of("200", "추천취소되었습니다.");
    }

    // 전체 글 조회(페이징)
    // tag 페이징 return도 추가
    // GlobalResponse에  ArticlePageResponse 담아서 보내주기
    @GetMapping("")
    @Operation(summary = "게시물 페이징", description = "게시물 페이징 시 사용하는 API")
    public GlobalResponse readAllPaging(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "tagName", required = false) String tagName,
            @RequestParam(value = "userNick", required = false) String nick
    ) {
        Page<ArticleListResponseDto> result;

        if(tagName == null) {
            result = articleService.searchAllPaging(pageNo);
        }
//        else if(nick != null) {
//            result = articleService.searchAllPagingByUser(pageNo,nick);
//        }
        else {
            result = articleService.searchAllPagingByTag(pageNo,tagName);
        }
        return GlobalResponse.of("200","success", result);
    }


    @GetMapping("/detail/db/{id}")
    @Operation(summary = "단일 글 db로 조회", description = "단일 글 조회 시 db만 사용하는 API")
    public GlobalResponse showArticleByDB(@PathVariable("id") Long id)  {
        Long memberId = 2L;
        Article article = articleService.getArticleById(id);
        ArticleDetailResponseDto articleDetailResponseDto = new ArticleDetailResponseDto(article);
        boolean isLiked = rq.isLoggedIn() && articleService.getLikeByArticleIdAndMemberId(article.getId(), memberId) != null;
        articleDetailResponseDto.setLikeValue(isLiked);
        return GlobalResponse.of("200", "success", articleDetailResponseDto);
    }


    @GetMapping("/detail/redis/{id}")
    @Operation(summary = "단일 글 redis로 조회", description = "단일 글 조회 시 redis만 사용하는 API")
    public GlobalResponse showArticleByRedis(@PathVariable("id") Long id)  {
        Long memberId = 2L;
        ArticleDetailResponseDto articleDetailResponseDto = articleService.findRecentArticle(memberId, id);
        if(articleDetailResponseDto != null) {
            articleDetailResponseDto.setLikeValue(
                    articleService.getLikeByArticleIdAndMemberId(articleDetailResponseDto.getId(), memberId) != null
            );
            return GlobalResponse.of("200", "success", articleDetailResponseDto);
        }
        else{
            articleService.saveRecentReadArticle(memberId, id);
            return GlobalResponse.of("200", "saved", articleDetailResponseDto);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/liked")
    @Operation(summary = "내가 추천한 게시물 목록", description = "내가 추천한 게시물 목록을 paging객체로 반환하는 API")
    public GlobalResponse showLikedArticles(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            Principal principal
    ) {
        Member member = memberService.findByUsername(principal.getName());
        Page<ArticleListResponseDto> likedArticlesPage = articleService.getLikedPaging(member, pageNo);
        return GlobalResponse.of("200", "success", likedArticlesPage);
    }
}