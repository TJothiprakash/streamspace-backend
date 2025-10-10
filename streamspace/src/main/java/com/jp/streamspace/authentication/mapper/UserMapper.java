package com.jp.streamspace.authentication.mapper;

import com.jp.streamspace.authentication.modal.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Insert("""
        INSERT INTO users(username, email, password, auth_provider, provider_id)
        VALUES (#{username}, #{email}, #{password}, #{authProvider}, #{providerId})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    void insertUser(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "reset_token", property = "resetToken"),
            @Result(column = "reset_token_expiry", property = "resetTokenExpiry")
    })
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE user_id = #{id}")
    User findById(Integer id);

    @Update("UPDATE users SET last_login = NOW() WHERE user_id = #{id}")
    void updateLastLogin(Integer id);

    // save reset token
    @Update("UPDATE users SET reset_token = #{token}, reset_token_expiry = #{expiry} WHERE user_id = #{userId}")
    void saveResetToken(@Param("userId") Integer userId, @Param("token") String token, @Param("expiry") LocalDateTime expiry);

    @Select("SELECT * FROM users WHERE reset_token = #{token}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "reset_token", property = "resetToken"),
            @Result(column = "reset_token_expiry", property = "resetTokenExpiry")
    })
    User findByResetToken(String token);

    // update password and clear token
    @Update("UPDATE users SET password = #{password}, reset_token = NULL, reset_token_expiry = NULL WHERE user_id = #{userId}")
    void updatePassword(User user);

}
