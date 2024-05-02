package com.ll.demo.comment.controller;

import com.ll.demo.comment.dto.comment.*;
import com.ll.demo.comment.dto.reply.CreateReplyCommentRequest;
import com.ll.demo.comment.dto.reply.DeleteReplyCommentResponse;
import com.ll.demo.comment.dto.reply.UpdateReplyCommentRequest;
import com.ll.demo.comment.dto.reply.UpdateReplyCommentResponse;
import com.ll.demo.comment.service.CommentService;
import com.ll.demo.global.response.GlobalResponse;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@Tag(name = "Comment", description = "Comment API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "요청에 실패했습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "요청이 거부되었습니다.", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "리소스를 서버에서 찾을 수 없습니다.", content = @Content(mediaType = "application/json"))
})
public class CommentController {
    private final CommentService commentService;
    private final MemberService memberService;

    @PostMapping("/")
    @Operation(summary = "댓글 생성", description = "댓글 생성 시 사용하는 API")
    public GlobalResponse<CreateCommentResponse> create(@Valid @RequestBody CreateCommentRequest createCommentRequest) {
        return GlobalResponse.of("201", "success", this.commentService.create(createCommentRequest));
    }

    @PutMapping("{id}")
    @Operation(summary = "댓글 수정", description = "댓글 수정 시 사용하는 API")
    public GlobalResponse<UpdateCommentResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCommentRequest updateCommentRequest
    ) {
        updateCommentRequest.setCommentId(id);
        return GlobalResponse.of("200", "success", this.commentService.update(updateCommentRequest));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "댓글 삭제", description = "댓글 삭제 시 사용하는 API")
    public GlobalResponse<DeleteCommentResponse> delete(
            @PathVariable("id") Long id,
            @Valid @RequestBody DeleteCommentRequest deleteCommentRequest
    ) {
        deleteCommentRequest.setCommentId(id);
        return GlobalResponse.of("200", "success", this.commentService.delete(deleteCommentRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{commentId}/reply")
    public GlobalResponse<CreateCommentResponse> createReply(
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CreateReplyCommentRequest createReplyCommentRequest,
            Principal principal
    ) {
        Member member = memberService.findByUsername(principal.getName());
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        }
        CreateCommentResponse response = this.commentService.createReply(commentId, createReplyCommentRequest, principal);
        return GlobalResponse.of("201", "success", response);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{commentId}/reply/{replyId}")
    public GlobalResponse<UpdateReplyCommentResponse> updateReply(
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId,
            @Valid @RequestBody UpdateReplyCommentRequest updateReplyCommentRequest,
            Principal principal
    ) {
        Member member = memberService.findByUsername(principal.getName());
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        }
        UpdateReplyCommentResponse response = commentService.updateReply(replyId, updateReplyCommentRequest, principal);
        return GlobalResponse.of("200", "success", response);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{commentId}/reply/{replyId}")
    public GlobalResponse<DeleteReplyCommentResponse> deleteReply(
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId,
            Principal principal
    ) {
        Member member = memberService.findByUsername(principal.getName());
        if (member == null) {
            return GlobalResponse.of("401", "로그인이 필요한 서비스입니다.");
        }
        DeleteReplyCommentResponse response = commentService.deleteReply(commentId, replyId, principal);
        return GlobalResponse.of("200", "success", response);
    }
}