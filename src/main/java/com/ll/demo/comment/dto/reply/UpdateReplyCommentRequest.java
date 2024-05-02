package com.ll.demo.comment.dto.reply;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateReplyCommentRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long replyId;

    private String username;
}
