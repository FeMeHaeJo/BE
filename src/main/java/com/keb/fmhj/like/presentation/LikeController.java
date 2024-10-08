package com.keb.fmhj.like.presentation;

import com.keb.fmhj.auth.utils.AccessTokenUtils;
import com.keb.fmhj.global.exception.ErrorCode;
import com.keb.fmhj.global.response.ApiResponse;
import com.keb.fmhj.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes/{postId}")
@Tag(name = "Like", description = "좋아요 관련 API")
public class LikeController {

    private final LikeService likeService;

    @PutMapping
    @Operation(summary = "좋아요 등록 API", description = "새로운 좋아요를 등록합니다.")
    public ApiResponse<Void> addLike(@PathVariable("postId") Long postId) {

        String loginId = AccessTokenUtils.isPermission();
        likeService.addLike(loginId, postId);
        return new ApiResponse<>(ErrorCode.REQUEST_OK);
    }
//
//    @DeleteMapping
//    @Operation(summary = "좋아요 삭제 API", description = "좋아요를 삭제합니다.")
//    public ApiResponse<Void> deleteLike(@PathVariable("postId") Long postId){
//
//        String loginId = AccessTokenUtils.isPermission();
//        likeService.deleteLike(loginId, postId);
//        return new ApiResponse<>(ErrorCode.REQUEST_OK);
//    }
}
