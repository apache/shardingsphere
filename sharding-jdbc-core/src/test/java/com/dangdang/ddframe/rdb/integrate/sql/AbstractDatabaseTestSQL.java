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

public abstract class AbstractDatabaseTestSQL implements DatabaseTestSQL {
    
    private static final String SELECT_COUNT_ALIAS_SQL = "SELECT COUNT(*) AS orders_count FROM t_order";
    
    private static final String SELECT_COUNT_SQL = "SELECT COUNT(*), COUNT(user_id) FROM t_order";
    
    private static final String SELECT_SUM_ALIAS_SQL = "SELECT SUM(user_id) AS user_id_sum FROM t_order";
    
    private static final String SELECT_SUM_SQL =  "SELECT SUM(user_id) FROM t_order";
    
    private static final String SELECT_MAX_ALIAS_SQL = "SELECT MAX(user_id) AS max_user_id FROM t_order";
    
    private static final String SELECT_MAX_SQL = "SELECT MAX(user_id) FROM t_order";
    
    private static final String SELECT_MIN_ALIAS_SQL = "SELECT MIN(user_id) AS min_user_id FROM t_order";
    
    private static final String SELECT_MIN_SQL = "SELECT MIN(user_id) FROM t_order";
    
    private static final String SELECT_AVG_ALIAS_SQL = "SELECT AVG(user_id) AS user_id_avg FROM t_order";
    
    private static final String SELECT_AVG_SQL = "SELECT AVG(user_id) FROM t_order";
    
    private static final String SELECT_COUNT_WITH_BINDING_TABLE_SQL = "SELECT COUNT(*) AS items_count FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.user_id IN (%s, %s) AND o.order_id BETWEEN %s AND %s";
    
    private static final String INSERT_WITH_ALL_PLACEHOLDERS_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
    
    private static final String INSERT_WITH_PARTIAL_PLACEHOLDERS_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, ?)";
    
    private static final String INSERT_WITHOUT_PLACEHOLDER_SQL = "INSERT INTO t_order (order_id, user_id, status) VALUES (%s, %s, 'insert')";
    
    private static final String INSERT_WITH_AUTO_INCREMENT_COLUMN_SQL = "INSERT INTO t_order (user_id, status) VALUES (%s, %s)";
    
    private static final String UPDATE_WITHOUT_ALIAS_SQL = "UPDATE t_order SET status = ? WHERE order_id = ? AND user_id = ?";
    
    private static final String UPDATE_WITH_ALIAS_SQL = "UPDATE t_order AS o SET o.status = ? WHERE o.order_id = ? AND o.user_id = ?";
    
    private static final String UPDATE_WITHOUT_SHARDING_VALUE_SQL = "UPDATE t_order SET status = ? WHERE status = ?";
    
    private static final String DELETE_WITHOUT_ALIAS_SQL = "DELETE FROM t_order WHERE order_id = ? AND user_id = ? AND status = ?";
    
    private static final String DELETE_WITHOUT_SHARDING_VALUE_SQL = "DELETE FROM t_order WHERE status = ?";
    
    private static final String ASSERT_SELECT_WITH_STATUS_SQL = "SELECT * FROM t_order WHERE status=?";
    
    @Override
    public String getSelectCountSql() {
        return SELECT_COUNT_SQL;
    }
    
    @Override
    public String getSelectCountAliasSql() {
        return SELECT_COUNT_ALIAS_SQL;
    }
    
    @Override
    public String getSelectSumSql() {
        return SELECT_SUM_SQL;
    }
    
    @Override
    public String getSelectSumAliasSql() {
        return SELECT_SUM_ALIAS_SQL;
    }
    
    @Override
    public String getSelectMaxSql() {
        return SELECT_MAX_SQL;
    }
    
    @Override
    public String getSelectMaxAliasSql() {
        return SELECT_MAX_ALIAS_SQL;
    }
    
    @Override
    public String getSelectMinSql() {
        return SELECT_MIN_SQL;
    }
    
    @Override
    public String getSelectMinAliasSql() {
        return SELECT_MIN_ALIAS_SQL;
    }
    
    @Override
    public String getSelectAvgSql() {
        return SELECT_AVG_SQL;
    }
    
    @Override
    public String getSelectAvgAliasSql() {
        return SELECT_AVG_ALIAS_SQL;
    }
    
    @Override
    public String getSelectCountWithBindingTableSql() {
        return SELECT_COUNT_WITH_BINDING_TABLE_SQL;
    }
    
    @Override
    public String getInsertWithAutoIncrementColumnSql() {
        return INSERT_WITH_AUTO_INCREMENT_COLUMN_SQL;
    }
    
    @Override
    public String getInsertWithAllPlaceholdersSql() {
        return INSERT_WITH_ALL_PLACEHOLDERS_SQL;
    }
    
    @Override
    public String getInsertWithPartialPlaceholdersSql() {
        return INSERT_WITH_PARTIAL_PLACEHOLDERS_SQL;
    }
    
    @Override
    public String getInsertWithoutPlaceholderSql() {
        return INSERT_WITHOUT_PLACEHOLDER_SQL;
    }
    
    @Override
    public String getUpdateWithoutAliasSql() {
        return UPDATE_WITHOUT_ALIAS_SQL;
    }
    
    @Override
    public String getUpdateWithAliasSql() {
        return UPDATE_WITH_ALIAS_SQL;
    }
    
    @Override
    public String getUpdateWithoutShardingValueSql() {
        return UPDATE_WITHOUT_SHARDING_VALUE_SQL;
    }
    
    @Override
    public String getDeleteWithoutAliasSql() {
        return DELETE_WITHOUT_ALIAS_SQL;
    }
    
    @Override
    public String getDeleteWithoutShardingValueSql() {
        return DELETE_WITHOUT_SHARDING_VALUE_SQL;
    }
    
    @Override
    public String getAssertSelectWithStatusSql() {
        return ASSERT_SELECT_WITH_STATUS_SQL;
    }
}
