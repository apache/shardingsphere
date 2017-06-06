CREATE SCHEMA IF NOT EXISTS `nullable_9`;

CREATE TABLE IF NOT EXISTS `t_order` (`order_id` INT NOT NULL, `user_id` INT NULL, `status` VARCHAR(45) NULL, PRIMARY KEY (`order_id`));
