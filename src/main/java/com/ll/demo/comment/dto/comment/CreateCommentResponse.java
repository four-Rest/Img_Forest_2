package com.ll.demo.comment.dto.comment;

import com.ll.demo.comment.entity.Comment;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CreateCommentResponse {
    private Long id;
    private String content;

    public static CreateCommentResponse of(Comment comment) {
        return CreateCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .build();
    }
}
