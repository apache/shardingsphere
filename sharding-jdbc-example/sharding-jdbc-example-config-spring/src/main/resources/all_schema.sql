CREATE SCHEMA IF NOT EXISTS `dbtbl_0`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_1`;

CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_0` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_1` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_2` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_3` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));

CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_0` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_1` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_2` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_3` (`order_item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(10) NULL, PRIMARY KEY (`order_item_id`));
