package com.jp.streamspace.vidoestream.mapper;

import com.jp.streamspace.vidoestream.modal.Video;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VideoMapper {


    @Insert("""
        INSERT INTO videos(
            uploaded_by, duration_seconds, created_at, updated_at, is_private,
            title, description, status, master_key, thumbnail_url
        ) VALUES(
            #{uploadedBy}, #{durationSeconds}, #{createdAt}, #{updatedAt}, #{isPrivate},
            #{title}, #{description}, #{status}, #{masterKey}, #{thumbnailUrl}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Video video);

    @Select("SELECT * FROM videos WHERE id = #{id}")
    Video findById(@Param("id") Integer id);

    @Update("UPDATE videos SET status = #{status}, updated_at = now() WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);


    @Update("UPDATE videos SET s3_key = #{s3Key}, updated_at = now() WHERE id = #{id}")
    int updateS3Key(@Param("id") Integer id, @Param("s3Key") String s3Key);
    @Update("UPDATE videos SET master_key = #{masterKey}, updated_at = now() WHERE id = #{id}")
    int updateMasterKey(@Param("id") Integer id, @Param("masterKey") String masterKey);

    @Select("""
    SELECT * FROM videos 
    WHERE uploaded_by = #{userId}
    ORDER BY created_at DESC
    LIMIT #{limit} OFFSET #{offset}
""")
    List<Video> findUserVideos(@Param("userId") int userId,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM videos WHERE uploaded_by = #{userId}")
    int countUserVideos(@Param("userId") int userId);


    @Select("""
    SELECT * FROM videos 
    WHERE is_private = false 
    ORDER BY created_at DESC
    LIMIT #{limit} OFFSET #{offset}
""")
    List<Video> findAllPublicVideos(@Param("limit") int limit,
                                    @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM videos WHERE is_private = false")
    int countPublicVideos();


    @Select("""
    SELECT * FROM videos 
    WHERE uploaded_by = #{userId} AND is_private = true
    ORDER BY created_at DESC
    LIMIT #{limit} OFFSET #{offset}
""")
    List<Video> findPrivateVideos(@Param("userId") int userId,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    @Select("""
    SELECT COUNT(*) FROM videos 
    WHERE uploaded_by = #{userId} AND is_private = true
""")
    int countPrivateVideos(@Param("userId") int userId);


}
