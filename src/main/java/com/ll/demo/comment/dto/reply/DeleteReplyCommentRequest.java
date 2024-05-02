package com.ll.demo.comment.dto.reply;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeleteReplyCommentRequest {
    @NotNull(message = "대댓글의 아이디는 필수입니다.")
    private Long replyId;

    @NotNull(message = "어떤 게시글의 아이디인지 확인이 필요합니다.")
    private Long articleId;

    @NotNull(message = "부모 댓글 아이디는 필수입니다.")
    private Long commentId;

    @NotNull(message = "어떤 멤버인지 확인이 필요합니다.")
    private String username;
}
