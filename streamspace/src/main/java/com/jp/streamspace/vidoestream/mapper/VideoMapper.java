package com.jp.streamspace.vidoestream.mapper;

import com.jp.streamspace.vidoestream.modal.Video;
import org.apache.ibatis.annotations.*;

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


}
