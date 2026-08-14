package org.example.learntrace;


import org.example.learntrace.mybatis.entity.User;
import org.example.learntrace.mybatis.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired
    private UsersMapper usersMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        // 1. 用 MyBatis 查库
        User user = usersMapper.selectByName(username);

        //查不到
        if(user == null){
            throw new UsernameNotFoundException(username + " not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getName())
                .password(user.getPassword_hash())
                .roles(user.getRole())
                .build();
    }
}
