/*
 Navicat Premium Dump SQL

 Source Server         : tengxunyun_MySQL
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 114.132.92.175:3306


 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 20/06/2026 14:01:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book`  (
  `book_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者',
  `ISBN` varchar(17) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ISBN',
  `classification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
  `inventory` int(11) NOT NULL DEFAULT 0 COMMENT '库存',
  `total_borrowing_times` bigint(20) NOT NULL DEFAULT 0 COMMENT '总借阅次数',
  PRIMARY KEY (`book_id`) USING BTREE,
  INDEX `idx_isbn`(`ISBN`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图书表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book
-- ----------------------------

-- ----------------------------
-- Table structure for book_img
-- ----------------------------
DROP TABLE IF EXISTS `book_img`;
CREATE TABLE `book_img`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `book_id` int(11) NOT NULL COMMENT '图书主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型（浏览器识别）',
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据存放（Base64或长文本存储）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图书封面表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_img
-- ----------------------------

-- ----------------------------
-- Table structure for book_location
-- ----------------------------
DROP TABLE IF EXISTS `book_location`;
CREATE TABLE `book_location`  (
  `location_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `book_id` int(11) NOT NULL COMMENT '图书编号',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存放地址',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '图书状态：0-在馆，1-借出，2-损毁，3-丢失',
  PRIMARY KEY (`location_id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实体书存放地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_location
-- ----------------------------

-- ----------------------------
-- Table structure for book_tag
-- ----------------------------
DROP TABLE IF EXISTS `book_tag`;
CREATE TABLE `book_tag`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `book_id` int(11) NOT NULL COMMENT '图书主键，链接book表的id',
  `tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图书标签',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_id`(`book_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图书标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_tag
-- ----------------------------

-- ----------------------------
-- Table structure for borrow_strategies
-- ----------------------------
DROP TABLE IF EXISTS `borrow_strategies`;
CREATE TABLE `borrow_strategies`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` tinyint(4) NOT NULL COMMENT '角色代码（对应读者类型：如1-学生，2-教师）',
  `max_books` tinyint(4) NOT NULL DEFAULT 0 COMMENT '可借册数',
  `max_days` int(11) NOT NULL DEFAULT 0 COMMENT '借阅天数',
  `renew_times` tinyint(4) NOT NULL DEFAULT 0 COMMENT '续借次数',
  `daily_limit` tinyint(4) NOT NULL DEFAULT -1 COMMENT '单日续借上限（-1表示不限制）',
  `daily_penalty` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '逾期罚金标准/天（教师不罚款，学生和访客设置独立的罚金标准）',
  `max_penalty_limit` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '罚金上限',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '借阅策略表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_strategies
-- ----------------------------

-- ----------------------------
-- Table structure for borrowing_record
-- ----------------------------
DROP TABLE IF EXISTS `borrowing_record`;
CREATE TABLE `borrowing_record`  (
  `borrow_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `location_id` int(11) NOT NULL COMMENT '图书主键（用位置的主键去指代特定条码的实体书）',
  `user_id` int(11) NOT NULL COMMENT '借阅人编号',
  `borrow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅时间',
  `due_return_time` datetime NULL DEFAULT NULL COMMENT '应归还时间',
  `actual_return_time` datetime NULL DEFAULT NULL COMMENT '实际归还时间',
  `penalty` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '处罚金额',
  `borrow_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '借阅状态：0-借阅中，1-已归还，2-逾期中，3-图书遗失',
  `payment_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '罚款缴纳状态：0-未缴费，1-已缴清，2-已免除/已核销（管理员通过高权限接口手动进行了核销）',
  PRIMARY KEY (`borrow_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_location_id`(`location_id`) USING BTREE,
  INDEX `idx_borrow_status`(`borrow_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '借阅记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrowing_record
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名（学工号）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（加密后的，如BCrypt存储，严禁明文）',
  `role_code` tinyint(4) NOT NULL COMMENT '角色代码（对应读者策略：如1-学生，2-教师）',
  `sys_role_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER' COMMENT '权力角色代码（Spring Security使用：SYS_ADMIN, LIBRARIAN, USER）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常，2-冻结（如欠费/违规），3-挂失',
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_code`(`user_code`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '张伟', '13800138001', 'U001', 'e10adc3949ba59abbe56e057f20f883e', 1, 'ROLE_ADMIN', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (2, '王芳', '13800138002', 'U002', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (3, '李娜', '13800138003', 'U003', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (4, '张敏', '13800138004', 'U004', 'e10adc3949ba59abbe56e057f20f883e', 3, 'ROLE_GUEST', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (5, '李静', '13800138005', 'U005', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (6, '王丽', '13800138006', 'U006', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (7, '刘洋', '13800138007', 'U007', 'e10adc3949ba59abbe56e057f20f883e', 3, 'ROLE_GUEST', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (8, '王磊', '13800138008', 'U008', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (9, '张杰', '13800138009', 'U009', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (10, '李强', '13800138010', 'U010', 'e10adc3949ba59abbe56e057f20f883e', 1, 'ROLE_ADMIN', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (11, '王军', '13800138011', 'U011', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (12, '张磊', '13800138012', 'U012', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (13, '李勇', '13800138013', 'U013', 'e10adc3949ba59abbe56e057f20f883e', 3, 'ROLE_GUEST', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (14, '刘艳', '13800138014', 'U014', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (15, '刘杰', '13800138015', 'U015', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (16, '李杰', '13800138016', 'U016', 'e10adc3949ba59abbe56e057f20f883e', 3, 'ROLE_GUEST', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (17, '张艳', '13800138017', 'U017', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (18, '王勇', '13800138018', 'U018', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (19, '王艳', '13800138019', 'U019', 'e10adc3949ba59abbe56e057f20f883e', 2, 'ROLE_USER', 1, 0, '2026-06-19 11:09:19');
INSERT INTO `user` VALUES (20, '李平', '13800138020', 'U020', 'e10adc3949ba59abbe56e057f20f883e', 3, 'ROLE_GUEST', 1, 0, '2026-06-19 11:09:19');

SET FOREIGN_KEY_CHECKS = 1;
