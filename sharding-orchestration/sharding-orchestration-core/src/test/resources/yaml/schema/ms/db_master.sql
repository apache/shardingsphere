DROP SCHEMA IF EXISTS `db_master`;
CREATE SCHEMA `db_master`;
DROP TABLE IF EXISTS `t_order`;
DROP TABLE IF EXISTS `t_order_item`;
DROP TABLE IF EXISTS `t_config`;
CREATE TABLE `t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE `t_order_item` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE `t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));
