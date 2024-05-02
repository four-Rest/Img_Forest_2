package com.ll.demo.comment.dto.comment;

import com.ll.demo.comment.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DeleteCommentResponse {
    private Long replyId;
    private LocalDateTime removedTime;
    public static DeleteCommentResponse of(Comment comment) {
        return DeleteCommentResponse.builder()
                .replyId(comment.getId())
                .removedTime(comment.getRemovedTime())
                .build();
    }
}
