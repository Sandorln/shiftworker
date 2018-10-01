package com.kinggo.app.shiftworker.view.community.service;

import com.kinggo.app.shiftworker.view.community.domain.Comment;
import com.kinggo.app.shiftworker.view.community.domain.Post;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author sangsik.kim
 */
public interface CommunityApi {
    @GET("posts/")
    Call<List<Post>> getPosts(@Query("page") Long page, @Query("searchType") String searchType, @Query("keyword") String keyword);

    @GET("posts/{id}?comments=true")
    Call<Post> getPosts(@Path("id") long id);

    @POST("posts/")
    Call<Post> writePost(@Body Post postDto, @Header("Authorization") String token);

    @POST("users/login/")
    Call<String> login(@Body UserInfo userDto);

    @POST("users/")
    Call<String> join(@Body UserInfo userDto);

    @POST("comments/")
    Call<Comment> addComment(@Body Comment comment, @Header("Authorization") String token);

    @GET("comments/")
    Call<List<Comment>> getComments(@Query("postId") Long postId);
}