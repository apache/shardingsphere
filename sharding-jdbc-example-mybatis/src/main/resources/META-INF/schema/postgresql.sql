CREATE DATABASE ds_0;
--connect to ds_0 
CREATE TABLE IF NOT EXISTS t_order_0 (order_id BIGINT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id BIGINT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_0 (item_id INT NOT NULL, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_1 (item_id INT NOT NULL, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id));

CREATE DATABASE ds_1;
--connect to ds_1
CREATE TABLE IF NOT EXISTS t_order_0 (order_id BIGINT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_1 (order_id BIGINT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
CREATE TABLE IF NOT EXISTS t_order_item_0 (item_id INT NOT NULL, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id));
CREATE TABLE IF NOT EXISTS t_order_item_1 (item_id INT NOT NULL, order_id BIGINT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id));
