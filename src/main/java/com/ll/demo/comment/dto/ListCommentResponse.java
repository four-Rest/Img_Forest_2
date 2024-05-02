package com.ll.demo.comment.dto;

import com.ll.demo.comment.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ListCommentResponse {
    private Long id;
    private Long commentId;
    private String content;
    private String username;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime removedTime;
    private List<ListCommentResponse> childComments = new ArrayList<>();

    public ListCommentResponse(Comment comment) {
        this.id = comment.getId();
        this.commentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        this.content = comment.getContent();
        this.username = comment.getMember().getUsername();
        this.createdDate = comment.getCreatedTime();
        this.modifiedDate = comment.getModifiedTime();
        this.removedTime = comment.getRemovedTime();
        this.childComments = comment.getChildComments().stream()
                .map(ListCommentResponse::new) // 재귀적으로 자식 댓글을 매핑합니다.
                .collect(Collectors.toList());
    }
}
