/*
Navicat MySQL Data Transfer

Date: 2022-08-01 12:32:56
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for v_video_edit_tab
-- ----------------------------
DROP TABLE IF EXISTS `v_video_edit_tab`;
CREATE TABLE `v_video_edit_tab` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `cmd` text NOT NULL COMMENT '执行命令',
  `msg` varchar(255) DEFAULT NULL COMMENT '执行信息',
  `path` varchar(255) NOT NULL DEFAULT '' COMMENT '输出路径',
  `ip` varchar(255) NOT NULL COMMENT '执行任务的服务器ip',
  `status` tinyint(3) NOT NULL DEFAULT '0' COMMENT '执行状态 [0-未开始] , [1-进行中], [2-执行成功], [3-执行失败], [4-执行异常]',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='音视频编辑记录表';
