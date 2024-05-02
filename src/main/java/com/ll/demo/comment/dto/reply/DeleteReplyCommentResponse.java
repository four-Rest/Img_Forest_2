package com.ll.demo.comment.dto.reply;

import com.ll.demo.comment.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeleteReplyCommentResponse {
    private Long replyId;
    private String content;
    private LocalDateTime removedTime;

    public static DeleteReplyCommentResponse of(Comment comment) {
        return DeleteReplyCommentResponse.builder()
                .replyId(comment.getId())
                .content(comment.getContent())
                .removedTime(comment.getRemovedTime())
                .build();
    }
}
