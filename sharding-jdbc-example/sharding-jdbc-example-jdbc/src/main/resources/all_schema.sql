CREATE SCHEMA IF NOT EXISTS `ds_jdbc_0`;
CREATE SCHEMA IF NOT EXISTS `ds_jdbc_1`;

CREATE TABLE IF NOT EXISTS `ds_jdbc_0`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(50), PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_0`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(50), PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_0`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_0`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));

CREATE TABLE IF NOT EXISTS `ds_jdbc_1`.`t_order_0` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(50), PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_1`.`t_order_1` (`order_id` INT NOT NULL, `user_id` INT NOT NULL, `status` VARCHAR(50), PRIMARY KEY (`order_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_1`.`t_order_item_0` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));
CREATE TABLE IF NOT EXISTS `ds_jdbc_1`.`t_order_item_1` (`item_id` INT NOT NULL, `order_id` INT NOT NULL, `user_id` INT NOT NULL, PRIMARY KEY (`item_id`));

INSERT INTO `ds_jdbc_0`.`t_order_0` VALUES (1000, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_0` VALUES (1002, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_0` VALUES (1004, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_0` VALUES (1006, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_0` VALUES (1008, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_item_0` VALUES (100001, 1000, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_0` VALUES (100201, 1002, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_0` VALUES (100401, 1004, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_0` VALUES (100601, 1006, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_0` VALUES (100801, 1008, 10);

INSERT INTO `ds_jdbc_0`.`t_order_1` VALUES (1001, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_1` VALUES (1003, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_1` VALUES (1005, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_1` VALUES (1007, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_1` VALUES (1009, 10, 'INIT');
INSERT INTO `ds_jdbc_0`.`t_order_item_1` VALUES (100101, 1001, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_1` VALUES (100301, 1003, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_1` VALUES (100501, 1005, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_1` VALUES (100701, 1007, 10);
INSERT INTO `ds_jdbc_0`.`t_order_item_1` VALUES (100901, 1009, 10);

INSERT INTO `ds_jdbc_1`.`t_order_0` VALUES (1100, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_0` VALUES (1102, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_0` VALUES (1104, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_0` VALUES (1106, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_0` VALUES (1108, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_item_0` VALUES (110001, 1100, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_0` VALUES (110201, 1102, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_0` VALUES (110401, 1104, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_0` VALUES (110601, 1106, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_0` VALUES (110801, 1108, 11);

INSERT INTO `ds_jdbc_1`.`t_order_1` VALUES (1101, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_1` VALUES (1103, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_1` VALUES (1105, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_1` VALUES (1107, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_1` VALUES (1109, 11, 'INIT');
INSERT INTO `ds_jdbc_1`.`t_order_item_1` VALUES (110101, 1101, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_1` VALUES (110301, 1103, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_1` VALUES (110501, 1105, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_1` VALUES (110701, 1107, 11);
INSERT INTO `ds_jdbc_1`.`t_order_item_1` VALUES (110901, 1109, 11);
