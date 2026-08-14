package org.example.learntrace.mybatis.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.learntrace.mybatis.entity.User;


@Mapper
public interface UsersMapper {
    User selectById(String id);
    User selectByName(String name);

    int deleteById(String id);
    int insert(User record);
    int update(@Param("id") Integer id,@Param("user") User user);
}
