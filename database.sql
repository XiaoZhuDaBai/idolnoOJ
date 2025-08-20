-- 创建数据库 & 指定字符集（推荐使用 utf8mb4 支持完整 Unicode）
CREATE DATABASE IF NOT EXISTS `oj`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `oj`;

-- ----------------------------
-- 表1：用户表 (user)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `uuid`             VARCHAR(255)  NOT NULL COMMENT '用户唯一ID',
    `openid`           VARCHAR(32)   DEFAULT NULL COMMENT '微信唯一标识',
    `unionid`          VARCHAR(32)   DEFAULT NULL COMMENT '微信开放平台统一ID',
    `auth`             VARCHAR(100)  DEFAULT 'user' COMMENT '用户权限: admin/user',
    `nickname`         VARCHAR(255)  DEFAULT NULL COMMENT '用户昵称',
    `school`           VARCHAR(100)  DEFAULT NULL COMMENT '学校',
    `course`           VARCHAR(100)  DEFAULT NULL COMMENT '专业',
    `email`            VARCHAR(255)  DEFAULT NULL COMMENT '邮箱（全局唯一）',
    `gender`           TINYINT(4)    DEFAULT NULL COMMENT '性别: 0女,1男',
    `avatar`           VARCHAR(255)  DEFAULT NULL COMMENT '头像URL',
    `signature`        VARCHAR(255)  DEFAULT NULL COMMENT '个性签名',
    `github`           VARCHAR(255)  DEFAULT NULL COMMENT 'GitHub地址',
    `status`           TINYINT(4)    DEFAULT 1 COMMENT '账号状态: 0注销, 1正常',
    `rate_website`     VARCHAR(255)  DEFAULT NULL COMMENT '常参加的竞赛网站',
    `rate_score`       INT(11)       DEFAULT 0 COMMENT '竞赛评分',
    `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`uuid`),
    UNIQUE INDEX `idx_openid` (`openid`),
    UNIQUE INDEX `idx_email` (`email`)
    ) ENGINE = InnoDB COMMENT = '用户信息表';

-- ----------------------------
-- 表2：题目信息表 (question)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `question` (
    `question_id`             VARCHAR(64)     NOT NULL COMMENT '题目唯一ID',
    `question_name`          VARCHAR(255)    NOT NULL COMMENT '题目名称',
    `question_type`          TINYINT(4)      DEFAULT 0 COMMENT '题型:0-ACM/1-OI',
    `add_time`               DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
    `author`                 VARCHAR(255)    DEFAULT '系统' COMMENT '作者',
    `difficulty`             TINYINT(4)      DEFAULT 1 COMMENT '难度:0-简单,1-中等,2-困难',
    `question_tag`           VARCHAR(255)    DEFAULT NULL COMMENT '题目标签(逗号分隔)',
    `time_limit`             BIGINT(64)      DEFAULT 1000 COMMENT '时间限制(ms)',
    `memory_limit`           BIGINT(64)      DEFAULT 256 COMMENT '内存限制(MB)',
    `stack_limit`            BIGINT(64)      DEFAULT 128 COMMENT '栈空间限制(MB)',
    `question_description`   TEXT            COMMENT '题目描述',
    `question_input_desc`    VARCHAR(255)    DEFAULT NULL COMMENT '输入描述',
    `question_output_desc`   VARCHAR(255)    DEFAULT NULL COMMENT '输出描述',
    `question_examples`      TEXT            COMMENT '样例输入输出(JSON格式)',
    `question_source`        VARCHAR(255)    DEFAULT '未知' COMMENT '题目来源',
    `question_commits`       INT(11)         DEFAULT 0 COMMENT '提交总次数',
    `ac`                     INT(11)         DEFAULT 0 COMMENT '通过次数',
    `modify_time`            DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `status`                 TINYINT(4)      DEFAULT 1 COMMENT '状态:0下架,1上架',
    PRIMARY KEY (`question_id`),
    INDEX `idx_difficulty` (`difficulty`)
    ) ENGINE=InnoDB COMMENT='题库信息表';

-- ----------------------------
-- 表3：用户提交记录表 (user_commit)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_commit` (
    `id`           INT UNSIGNED      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `uid`          VARCHAR(255)      NOT NULL COMMENT '用户ID（关联user.uuid）',
    `qid`          VARCHAR(64)       NOT NULL COMMENT '题目ID（关联question.question_id）',
    `commit_id`    VARCHAR(255)      DEFAULT NULL COMMENT '沙箱提交唯一ID（可关联至测试结果）',
    `code`         TEXT              NOT NULL COMMENT '提交的代码',
    `language`     VARCHAR(128)      NOT NULL COMMENT '编程语言:Java/Python...',
    `create_time`  DATETIME          DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `modify_time`  DATETIME          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后状态更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_commit_id` (`commit_id`),
    INDEX `idx_uid_qid` (`uid`, `qid`),
    FOREIGN KEY (`uid`) REFERENCES `user` (`uuid`) ON DELETE CASCADE,
    FOREIGN KEY (`qid`) REFERENCES `question` (`question_id`) ON DELETE CASCADE
    ) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='用户提交日志表';

-- ----------------------------
-- 表4：测试案例表 (commit_case)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `commit_case` (
    `commit_id`     VARCHAR(255)     NOT NULL COMMENT '关联至沙箱的提交ID',
    `cn_name`       TEXT             COMMENT '评测结果中文描述（如：通过/超时）',
    `english_name`  TEXT             COMMENT '评测结果英文描述（如：Accepted/Time Limit Exceeded）',
    `input`         TEXT             COMMENT '输入案例（存储用例文件的路径或JSON内容）',
    `output`        TEXT             COMMENT '期望输出结果',
    `time`          BIGINT(64)       DEFAULT 0 COMMENT '实际耗时(ms)',
    `memory`        BIGINT(64)       DEFAULT 0 COMMENT '峰值内存使用(KB)',
    PRIMARY KEY (`commit_id`)
    ) ENGINE=InnoDB COMMENT='判题用例与结果表';
