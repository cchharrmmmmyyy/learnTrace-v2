package org.example.learntrace.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.example.learntrace.BusinessException;
import org.example.learntrace.mybatis.entity.ArticleContentVO;
import org.example.learntrace.mybatis.entity.ArticleDetail;
import org.example.learntrace.mybatis.entity.ArticleDetailVO;
import org.example.learntrace.mybatis.entity.ArticleSimpleVO;
import org.example.learntrace.mybatis.entity.QuestionVO;
import org.example.learntrace.mybatis.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static org.example.learntrace.ErrorCode.*;

import java.util.Collections;
import java.util.List;

@Service
public class ArticleService {
    @Autowired
    ArticleMapper articleMapper;

    //专用于解析 DB 中 snake_case 的 JSON 列（question_id -> questionId）
    //忽略未知字段（如 correct_answer，QuestionVO 里故意没有它，天然防泄露）
    private final ObjectMapper jsonMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<ArticleSimpleVO> getArticle(String title, String difficulty, String theme) {
        if(difficulty != null && !List.of("easy", "medium", "hard").contains(difficulty)){
            throw new BusinessException(PARAM_INVALID, HttpStatus.BAD_REQUEST,"难度参数不合法");
        }

        List<ArticleSimpleVO> articleSimpleVOList = articleMapper.queryArticles(title, difficulty, theme);
        if(articleSimpleVOList == null){
            return Collections.emptyList();
        }
        return articleSimpleVOList;
    }

    public ArticleDetailVO getArticleDetail(String articleId) {
        ArticleDetail detail = articleMapper.selectDetailById(articleId);
        if(detail == null){
            throw new BusinessException(ARTICLE_NOT_FOUND, HttpStatus.NOT_FOUND,"文章不存在");
        }

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(detail.getId());
        vo.setTitle(detail.getTitle());
        vo.setWordCount(detail.getWordCount());
        vo.setDifficulty(detail.getDifficulty());
        vo.setTheme(detail.getTheme());

        try {
            //正文 NOT NULL，直接解析
            vo.setContent(jsonMapper.readValue(detail.getContent(), ArticleContentVO.class));
            //题目可空，空串/空值则返回 null
            if(detail.getQuestions() != null && !detail.getQuestions().isBlank()){//isBlank的意思就是没有字符，空字符，换行等等都会返回true
                vo.setQuestions(jsonMapper.readValue(detail.getQuestions(), new TypeReference<List<QuestionVO>>() {}));
            }
        } catch (JsonProcessingException e) {
            //DB 里存的 JSON 不符合规范，属于数据问题
            throw new BusinessException(SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,"文章数据解析失败");
        }
        return vo;
    }
}
