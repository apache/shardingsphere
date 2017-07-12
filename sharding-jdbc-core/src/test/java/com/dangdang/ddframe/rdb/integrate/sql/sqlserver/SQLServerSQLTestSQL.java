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

package com.dangdang.ddframe.rdb.integrate.sql.sqlserver;

import com.dangdang.ddframe.rdb.integrate.sql.AbstractDatabaseTestSQL;

public final class SQLServerSQLTestSQL extends AbstractDatabaseTestSQL {
    
    private static final String SELECT_PAGING_WITH_OFFSET_AND_ROW_COUNT_SQL = "SELECT * FROM"
            + " (SELECT TOP (%s) row_number() OVER (ORDER BY i.item_id DESC) AS rownum_, i.item_id, o.order_id as order_id, o.status as status, o.user_id as user_id"
            + " FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.user_id IN (%s, %s) AND o.order_id BETWEEN %s AND %s) AS row_"
            + " WHERE row_.rownum_ > %s";
    
    private static final String SELECT_PAGING_WITH_ROW_COUNT_SQL = "SELECT * FROM"
            + " (SELECT TOP (%s) row_number() OVER (ORDER BY i.item_id DESC) AS rownum_, i.item_id, o.order_id as order_id, o.status as status, o.user_id as user_id"
            + " FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.user_id IN (%s, %s) AND o.order_id BETWEEN %s AND %s) AS row_";
    
    private static final String SELECT_PAGING_WITH_OFFSET_SQL = "SELECT * FROM"
            + " (SELECT TOP (%s) row_number() OVER (ORDER BY i.item_id DESC) AS rownum_, i.item_id, o.order_id as order_id, o.status as status, o.user_id as user_id"
            + " FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.user_id IN (%s, %s) AND o.order_id BETWEEN %s AND %s) AS row_"
            + " WHERE row_.rownum_ > %s";
    
    private static final String SELECT_GROUP_BY_USER_ID_SQL = "SELECT user_id AS uid FROM t_order GROUP BY user_id";
    
    @Override
    public String getSelectPagingWithOffsetAndRowCountSql() {
        return SELECT_PAGING_WITH_OFFSET_AND_ROW_COUNT_SQL;
    }
    
    @Override
    public String getSelectPagingWithRowCountSql() {
        return SELECT_PAGING_WITH_ROW_COUNT_SQL;
    }
    
    @Override
    public String getSelectPagingWithOffsetSql() {
        return SELECT_PAGING_WITH_OFFSET_SQL;
    }
    
    @Override
    public String getSelectGroupByUserIdSql() {
        return SELECT_GROUP_BY_USER_ID_SQL;
    }
}
