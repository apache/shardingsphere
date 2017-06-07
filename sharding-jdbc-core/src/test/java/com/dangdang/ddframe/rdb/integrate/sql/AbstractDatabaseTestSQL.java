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
            + " WHERE o.user_id IN (?, ?) AND o.order_id BETWEEN ? AND ?";
    
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
}
