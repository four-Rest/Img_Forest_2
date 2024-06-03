package com.ll.demo.comment.service;

import com.ll.demo.article.entity.Article;
import com.ll.demo.article.repository.ArticleRepository;
import com.ll.demo.comment.dto.comment.*;
import com.ll.demo.comment.dto.reply.CreateReplyCommentRequest;
import com.ll.demo.comment.dto.reply.DeleteReplyCommentResponse;
import com.ll.demo.comment.dto.reply.UpdateReplyCommentRequest;
import com.ll.demo.comment.dto.reply.UpdateReplyCommentResponse;
import com.ll.demo.comment.entity.Comment;
import com.ll.demo.comment.repository.CommentRepository;
import com.ll.demo.member.entity.Member;
import com.ll.demo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateCommentResponse create(CreateCommentRequest request) {
        Member member = this.verifyMember(request.getUsername());
        Article article = this.verifyArticle(request.getArticleId());

        Comment saved = this.commentRepository.save(CreateCommentRequest.toEntity(request, member, article));

        return CreateCommentResponse.of(saved);
    }

    @Transactional
    public UpdateCommentResponse update(UpdateCommentRequest request) {
        Member member = this.verifyMember(request.getUsername());
        Article article = this.verifyArticle(request.getArticleId());

        Comment saveComment = this.verifyComment(request.getCommentId());
        saveComment.changeComment(UpdateCommentRequest.toEntity(request, member, article));

        return UpdateCommentResponse.of(this.commentRepository.save(saveComment));
    }

    @Transactional
    public DeleteCommentResponse delete(DeleteCommentRequest request) {
        this.verifyMember(request.getUsername());
        this.verifyArticle(request.getArticleId());

        Comment saveComment = this.verifyComment(request.getCommentId());
//        saveComment.setRemovedTime(LocalDateTime.now());
        commentRepository.delete(saveComment);

        return DeleteCommentResponse.of(saveComment);
    }

    @Transactional
    public CreateCommentResponse createReply(Long commentId, CreateReplyCommentRequest request, Principal principal) {
        Member member = this.verifyMember(principal.getName());

        Comment saved = this.commentRepository.save(CreateReplyCommentRequest.toEntity(request, member));

        Comment parentComment = verifyComment(commentId);
        saved.setParentComment(parentComment);

        return CreateCommentResponse.of(saved);
    }

    @Transactional
    public UpdateReplyCommentResponse updateReply(Long replyId, UpdateReplyCommentRequest request, Principal principal) {
        Member member = verifyMember(principal.getName());

        Comment replyComment = verifyReplyComment(replyId);
        if (!replyComment.getMember().equals(member)) {
            throw new IllegalArgumentException("해당 대댓글을 수정할 권한이 없습니다.");
        }
        replyComment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(replyComment);

        return UpdateReplyCommentResponse.of(updatedComment);
    }

    @Transactional
    public DeleteReplyCommentResponse deleteReply(Long commentId, Long replyId, Principal principal) {
        Member member = verifyMember(principal.getName());

        Comment replyComment = verifyReplyComment(replyId);
        if (!replyComment.getMember().equals(member)) {
            throw new IllegalArgumentException("해당 대댓글을 삭제할 권한이 없습니다.");
        }

        Comment parentComment = verifyComment(commentId);
        parentComment.getChildComments().remove(replyComment);
        replyComment.setRemovedTime(LocalDateTime.now());

        commentRepository.save(replyComment);

        return DeleteReplyCommentResponse.of(replyComment);
    }

    private Member verifyMember(String username) {
        return this.memberRepository.findByUsername(username).orElseThrow(() ->
                new IllegalArgumentException("멤버가 존재하지 않습니다.")
        );
    }

    private Article verifyArticle(Long id) {
        return this.articleRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
    }

    private Comment verifyComment(Long id) {
        return this.commentRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("댓글이 존재하지 않습니다.")
        );
    }

    private Comment verifyReplyComment(Long replyId) {
        return this.commentRepository.findById(replyId).orElseThrow(() ->
                new IllegalArgumentException("대댓글이 존재하지 않습니다.")
        );
    }
}