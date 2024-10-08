package com.keb.fmhj.comment.presentation;

import com.keb.fmhj.auth.utils.AccessTokenUtils;
import com.keb.fmhj.comment.domain.dto.request.AddCommentDto;
import com.keb.fmhj.comment.domain.dto.request.UpdateCommentDto;
import com.keb.fmhj.comment.domain.dto.response.CommentDetailDto;
import com.keb.fmhj.comment.service.CommentService;
import com.keb.fmhj.global.exception.ErrorCode;
import com.keb.fmhj.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    @Operation(summary = "댓글 등록 API", description = "새로운 댓글을 등록합니다.")
    public ApiResponse<Void> createComment(@Valid @RequestBody AddCommentDto addDto,
                                           @PathVariable Long postId) {

        String loginId = AccessTokenUtils.isPermission();
        commentService.createComment(addDto, postId, loginId);
        return new ApiResponse<>(ErrorCode.REQUEST_OK);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "전체 댓글 조회 API", description = "해당 게시글의 모든 댓글들을 조회합니다.")
    public ApiResponse<CommentDetailDto> getAllComments(@PathVariable Long postId) {

        List<CommentDetailDto> comments = commentService.getAllCommentDetails(postId);
        return new ApiResponse<>(comments);
    }

    @PutMapping("/{postId}/{commentId}")
    @Operation(summary = "댓글 수정 API", description = "특정 댓글의 내용을 수정합니다.")
    public ApiResponse<Void> updateComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @RequestBody UpdateCommentDto updateDto) {

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.updateComment(loginId, postId, commentId, updateDto);
        return new ApiResponse<>(ErrorCode.REQUEST_OK);
    }

    @DeleteMapping("/{postId}/{commentId}")
    @Operation(summary = "댓글 삭제 API", description = "댓글을 삭제합니다.")
    public ApiResponse<Void> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId) {

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(commentId, loginId);
        return new ApiResponse<>(ErrorCode.REQUEST_OK);
    }
}
