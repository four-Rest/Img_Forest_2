package com.ll.demo.comment.dto.comment;

import com.ll.demo.article.entity.Article;
import com.ll.demo.comment.entity.Comment;
import com.ll.demo.member.entity.Member;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Validated
public class CreateCommentRequest {
    @NotNull(message = "어떤 게시글의 아이디인지 확인이 필요합니다.")
    private Long articleId;

    @NotNull(message = "어떤 멤버인지 확인이 필요합니다.")
    private String username;

    @NotEmpty(message = "댓글 내용은 필수입니다.")
    private String content;

    public static Comment toEntity(CreateCommentRequest request, Member member, Article article) {
        return Comment.builder()
                .content(request.getContent())
                .member(member)
                .article(article)
                .build();
    }
}