package org.example.learntrace.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.learntrace.mybatis.entity.ArticleDetail;
import org.example.learntrace.mybatis.entity.ArticleSimpleVO;

import java.util.List;
@Mapper
public interface ArticleMapper {
    List<ArticleSimpleVO> queryArticles(
            @Param("title") String title,
            @Param("difficulty") String difficulty,
            @Param("theme") String theme
    );

    ArticleDetail selectDetailById(@Param("articleId") String articleId);
}
