-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE  IF NOT EXISTS lt_user (
                         user_id       VARCHAR(32)  NOT NULL COMMENT '用户ID，使用UUID',
                         user_name     VARCHAR(64)  NOT NULL COMMENT '用户名',
                         password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                         role          ENUM('user', 'admin')
                                                    NOT NULL DEFAULT 'user' COMMENT '用户角色',
                         create_time   BIGINT       NOT NULL COMMENT '创建时间，Unix毫秒时间戳',

                         PRIMARY KEY (user_id),
                         UNIQUE KEY uk_lt_user_name (user_name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '用户表';


-- =====================================================
-- 2. 文章表
-- =====================================================
CREATE TABLE  IF NOT EXISTS lt_article (
                            article_id  VARCHAR(32)  NOT NULL COMMENT '文章ID，如 art_001',
                            title       VARCHAR(255) NOT NULL COMMENT '文章标题',
                            word_count  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文章总词数',
                            difficulty  VARCHAR(32)  NOT NULL COMMENT '难度等级',
                            content     JSON         NOT NULL COMMENT '段落、句子和单词结构',
                            questions   JSON         NULL COMMENT '文章题目列表',

                            PRIMARY KEY (article_id),
                            KEY idx_lt_article_difficulty (difficulty)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '文章表';


-- =====================================================
-- 3. 主题表
-- =====================================================
CREATE TABLE  IF NOT EXISTS lt_theme (
                            theme_id  VARCHAR(32)  NOT NULL COMMENT '主题ID，如 theme_001',
                            name      VARCHAR(64)  NOT NULL COMMENT '主题名称',

                            PRIMARY KEY (theme_id),
                            UNIQUE KEY uk_lt_theme_name (name)

) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '主题表';


-- =====================================================
-- 4. 文章-主题关联表（多对多）
-- =====================================================
CREATE TABLE  IF NOT EXISTS lt_article_theme (
                            article_id  VARCHAR(32) NOT NULL COMMENT '文章ID',
                            theme_id    VARCHAR(32) NOT NULL COMMENT '主题ID',

                            PRIMARY KEY (article_id, theme_id),
                            KEY idx_lt_article_theme_theme (theme_id),

                            CONSTRAINT fk_lt_article_theme_article
                                FOREIGN KEY (article_id)
                                    REFERENCES lt_article (article_id)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_lt_article_theme_theme
                                FOREIGN KEY (theme_id)
                                    REFERENCES lt_theme (theme_id)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE

) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '文章主题关联表';


-- =====================================================
-- 5. 阅读会话表
-- =====================================================
CREATE TABLE  IF NOT EXISTS lt_session (
                            session_id      VARCHAR(64) NOT NULL COMMENT '会话ID，如 sess_20260427_001',
                            user_id         VARCHAR(32) NOT NULL COMMENT '用户ID',
                            article_id      VARCHAR(32) NOT NULL COMMENT '文章ID',
                            start_time      BIGINT      NOT NULL COMMENT '开始时间，Unix毫秒时间戳',
                            end_time        BIGINT      NULL COMMENT '结束时间，未结束时为NULL',
                            raw_events      JSON        NULL COMMENT '滚动、停留、标注、答题等原始事件',
                            analysis_results JSON       NULL COMMENT '阅读分析结果',
                            report          JSON        NULL COMMENT '阅读报告',

                            PRIMARY KEY (session_id),

                            KEY idx_lt_session_user_time (user_id, start_time),
                            KEY idx_lt_session_article_time (article_id, start_time),

                            CONSTRAINT fk_lt_session_user
                                FOREIGN KEY (user_id)
                                    REFERENCES lt_user (user_id)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_lt_session_article
                                FOREIGN KEY (article_id)
                                    REFERENCES lt_article (article_id)
                                    ON UPDATE CASCADE
                                    ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT = '阅读会话表';
