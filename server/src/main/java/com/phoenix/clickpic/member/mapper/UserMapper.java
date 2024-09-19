package com.phoenix.clickpic.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.phoenix.clickpic.member.model.User;


@Mapper
public interface UserMapper {

    //@Insert("INSERT INTO users (user_id, nickname, status, created_at) VALUES (#{userId}, #{nickname}, #{status}, #{createdAt})")
    void insertUser(User user);

   // @Update("UPDATE users SET nickname = #{nickname}, status = #{status} WHERE user_id = #{userId}")
    void updateUser(User user);

   // @Select("SELECT COUNT(*) FROM users WHERE user_id = #{userId}")
    int checkUserExists(String userId);

    @Update("UPDATE users SET status = 'N' WHERE user_id = #{userId}")
    void deactivateUser(String userId);
    
    // userId로 닉네임 가져오기
    String getNicknameByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM users WHERE NICKNAME = #{nickname}")
    User findByNickname(@Param("nickname") String nickname);
    
    //@Select("SELECT * FROM users WHERE USER_ID = #{userId}")
    User findByUserId(@Param("userId") String userId); // 유저 아이디로 사용자 조회

}

