package com.ll.demo.comment.dto.reply;

import com.ll.demo.comment.entity.Comment;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UpdateReplyCommentResponse {
    private Long replyId;
    private String content;

    public static UpdateReplyCommentResponse of(Comment comment) {
        return UpdateReplyCommentResponse.builder()
                .replyId(comment.getId())
                .content(comment.getContent())
                .build();
    }
}