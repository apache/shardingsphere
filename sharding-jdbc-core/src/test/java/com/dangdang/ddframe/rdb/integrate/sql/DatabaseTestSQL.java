/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.sql;

public class DatabaseTestSQL {
    
    public static final String INSERT_ORDER_WITH_ALL_PLACEHOLDERS_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
    
    public static final String INSERT_ORDER_ITEM_WITH_ALL_PLACEHOLDERS_SQL = "INSERT INTO t_order_item (item_id, order_id, user_id, status) VALUES (?, ?, ?, ?)";
    
    public static final String INSERT_WITH_PARTIAL_PLACEHOLDERS_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, ?)";
    
    public static final String INSERT_WITH_AUTO_INCREMENT_COLUMN_SQL = "INSERT INTO t_order_item (order_id, user_id, status) VALUES (%s, %s, %s)";
    
    public static final String INSERT_WITHOUT_PLACEHOLDER_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, 'insert')";
    
    public static final String UPDATE_WITHOUT_ALIAS_SQL = "UPDATE t_order SET status = %s WHERE order_id = %s AND user_id = %s";
    
    public static final String UPDATE_WITH_ALIAS_SQL = "UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?";
    
    public static final String UPDATE_WITHOUT_SHARDING_VALUE_SQL = "UPDATE t_order SET status = %s WHERE status = %s";
    
    public static final String DELETE_WITHOUT_ALIAS_SQL = "DELETE FROM t_order WHERE order_id = %s AND user_id = %s AND status = %s";
    
    public static final String DELETE_WITHOUT_SHARDING_VALUE_SQL = "DELETE FROM t_order WHERE status = %s";
    
    public static final String ASSERT_SELECT_WITH_STATUS_SQL = "SELECT * FROM t_order WHERE status = %s ORDER BY order_id";
    
    public static final String SELECT_EQUALS_WITH_SINGLE_TABLE_SQL = "SELECT * FROM t_order WHERE user_id = %s AND order_id = %s";
    
    public static final String SELECT_BETWEEN_WITH_SINGLE_TABLE_SQL = "SELECT * FROM t_order WHERE user_id BETWEEN %s AND %s AND order_id BETWEEN %s AND %s ORDER BY user_id, order_id";
    
    public static final String SELECT_IN_WITH_SINGLE_TABLE_SQL = "SELECT * FROM t_order WHERE user_id IN (%s, %s, %s) AND order_id IN (%s, %s) ORDER BY user_id, order_id";
    
    public static final String SELECT_COUNT_AS_ORDERS_COUNT_SQL = "SELECT COUNT(*) AS orders_count FROM t_order WHERE status = ?";
    
    public static final String SELECT_WITH_AUTO_INCREMENT_COLUMN_SQL = "SELECT item_id from t_order_item where user_id = %d and order_id= %s and status = 'BATCH'";
    
    public static final String SELECT_WITH_ALIAS_SQL = "SELECT user_id AS usr_id FROM t_order WHERE status = 'init'";
}
