/**
 * 分库分表.
 */
CREATE SCHEMA IF NOT EXISTS `dbtbl_0`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_1`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_2`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_3`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_4`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_5`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_6`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_7`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_8`;
CREATE SCHEMA IF NOT EXISTS `dbtbl_9`;

CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));
CREATE TABLE IF NOT EXISTS `dbtbl_0`.`t_global` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));


CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_1`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_2`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_3`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_4`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_5`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_6`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_7`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_8`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `dbtbl_9`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

/**
 * 仅分库.
 */
CREATE SCHEMA IF NOT EXISTS `db_0`;
CREATE SCHEMA IF NOT EXISTS `db_1`;
CREATE SCHEMA IF NOT EXISTS `db_2`;
CREATE SCHEMA IF NOT EXISTS `db_3`;
CREATE SCHEMA IF NOT EXISTS `db_4`;
CREATE SCHEMA IF NOT EXISTS `db_5`;
CREATE SCHEMA IF NOT EXISTS `db_6`;
CREATE SCHEMA IF NOT EXISTS `db_7`;
CREATE SCHEMA IF NOT EXISTS `db_8`;
CREATE SCHEMA IF NOT EXISTS `db_9`;

CREATE TABLE IF NOT EXISTS `db_0`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_1`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_2`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_3`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_4`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_5`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_6`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_7`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_8`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_9`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_0`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_1`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_2`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_3`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_4`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_5`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_6`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_7`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_8`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_9`.`t_order_item` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));

/**
 * 仅分表.
 */
CREATE SCHEMA IF NOT EXISTS `db_single`;

CREATE TABLE IF NOT EXISTS `db_single`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `db_single`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

/**
 * nullable测试.
 */
CREATE SCHEMA IF NOT EXISTS `nullable_0`;

CREATE TABLE IF NOT EXISTS `nullable_0`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_1`;

CREATE TABLE IF NOT EXISTS `nullable_1`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_2`;

CREATE TABLE IF NOT EXISTS `nullable_2`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_3`;

CREATE TABLE IF NOT EXISTS `nullable_3`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_4`;

CREATE TABLE IF NOT EXISTS `nullable_4`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_5`;

CREATE TABLE IF NOT EXISTS `nullable_5`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_6`;

CREATE TABLE IF NOT EXISTS `nullable_6`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_7`;

CREATE TABLE IF NOT EXISTS `nullable_7`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_8`;

CREATE TABLE IF NOT EXISTS `nullable_8`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

CREATE SCHEMA IF NOT EXISTS `nullable_9`;

CREATE TABLE IF NOT EXISTS `nullable_9`.`t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));

/**
 * 读写分离测试.
 */
CREATE SCHEMA IF NOT EXISTS `master_0`;

CREATE TABLE IF NOT EXISTS `master_0`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_0`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `master_0`.`t_global` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_1`;

CREATE TABLE IF NOT EXISTS `master_1`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_1`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_2`;

CREATE TABLE IF NOT EXISTS `master_2`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_2`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_3`;

CREATE TABLE IF NOT EXISTS `master_3`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_3`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_4`;

CREATE TABLE IF NOT EXISTS `master_4`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_4`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_5`;

CREATE TABLE IF NOT EXISTS `master_5`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_5`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_6`;

CREATE TABLE IF NOT EXISTS `master_6`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_6`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_7`;

CREATE TABLE IF NOT EXISTS `master_7`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_7`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_8`;

CREATE TABLE IF NOT EXISTS `master_8`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_8`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `master_9`;

CREATE TABLE IF NOT EXISTS `master_9`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `master_9`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_0`;

CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_0`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `slave_0`.`t_global` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_1`;

CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_1`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_2`;

CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_2`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_3`;

CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_3`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_4`;

CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_4`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_5`;

CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_5`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_6`;

CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_6`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_7`;

CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_7`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_8`;

CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_8`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));

CREATE SCHEMA IF NOT EXISTS `slave_9`;

CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_2` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_3` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_4` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_5` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_6` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_7` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_8` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_9` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_2` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_3` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_4` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_5` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_6` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_7` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_8` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_order_item_9` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `slave_9`.`t_config` (`id` INT NOT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`id`));
