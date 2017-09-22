CREATE SCHEMA IF NOT EXISTS `ds_0`;
CREATE SCHEMA IF NOT EXISTS `ds_1`;

CREATE TABLE IF NOT EXISTS `ds_0`.`t_order_0` (`order_id` BIGINT PRIMARY KEY AUTO_INCREMENT, `user_id` INT NOT NULL, `status` VARCHAR(50));
CREATE TABLE IF NOT EXISTS `ds_0`.`t_order_1` (`order_id` BIGINT PRIMARY KEY AUTO_INCREMENT, `user_id` INT NOT NULL, `status` VARCHAR(50));
CREATE TABLE IF NOT EXISTS `ds_0`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `ds_0`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));

CREATE TABLE IF NOT EXISTS `ds_1`.`t_order_0` (`order_id` BIGINT PRIMARY KEY AUTO_INCREMENT, `user_id` INT NOT NULL, `status` VARCHAR(50));
CREATE TABLE IF NOT EXISTS `ds_1`.`t_order_1` (`order_id` BIGINT PRIMARY KEY AUTO_INCREMENT, `user_id` INT NOT NULL, `status` VARCHAR(50));
CREATE TABLE IF NOT EXISTS `ds_1`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `ds_1`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` BIGINT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));
