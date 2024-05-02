package com.ll.demo.comment.dto.reply;

import com.ll.demo.comment.entity.Comment;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReplyCommentResponse {
    private Long replyId;
    private String content;

    public static CreateReplyCommentResponse of(Comment comment) {
        return CreateReplyCommentResponse.builder()
                .replyId(comment.getId())
                .content(comment.getContent())
                .build();
    }
}
