package com.ll.demo.comment.dto;

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
public class DeleteCommentRequest {
    @Setter
    private Long commentId;

    @NotNull(message = "어떤 게시글의 아이디인지 확인이 필요합니다.")
    private Long articleId;

    @NotNull(message = "어떤 멤버인지 확인이 필요합니다.")
    private Long memberId;

    @Setter
    @NotEmpty(message = "댓글 내용은 필수입니다.")
    private String content;

    public static Comment toEntity(DeleteCommentRequest request, Member member, Article article) {
        return Comment.builder()
                .id(request.getCommentId())
                .content(request.getContent())
                .member(member)
                .article(article)
                .build();
    }
}
