package com.keb.fmhj.post.domain.dto.response;

import com.keb.fmhj.post.domain.Category;
import com.keb.fmhj.post.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailDto {

    private Long postId;
    private String title;
    private Long likeCount;
    private Category category;
    private String profileImage;
    private String nickName;
    private LocalDateTime createdAt;

    public static PostDetailDto toDto(Post post) {
        return PostDetailDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .likeCount(post.getLikeCount())
                .category(post.getCategory())
                .profileImage(post.getMember().getProfileImage())
                .nickName(post.getMember().getNickName())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
