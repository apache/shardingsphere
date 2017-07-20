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

package com.dangdang.ddframe.rdb.integrate.sql.oracle;

import com.dangdang.ddframe.rdb.integrate.sql.AbstractDatabaseTestSQL;

public final class OracleSQLTestSQL extends AbstractDatabaseTestSQL {
    
    private static final String SELECT_PAGING_WITH_OFFSET_AND_ROW_COUNT_SQL = "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT"
            + " order0_.order_id as order_id, order0_.status as status, order0_.user_id as user_id"
            + " FROM t_order order0_ JOIN t_order_item i ON order0_.user_id = i.user_id AND order0_.order_id = i.order_id"
            + " WHERE order0_.user_id IN (%s, %s) AND order0_.order_id BETWEEN %s AND %s ORDER BY i.item_id DESC) row_ WHERE rownum <= %s) t WHERE t.rownum_ > %s";
    
    private static final String SELECT_PAGING_WITH_ROW_COUNT_SQL = "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT"
            + " order0_.order_id as order_id, order0_.status as status, order0_.user_id as user_id"
            + " FROM t_order order0_ JOIN t_order_item i ON order0_.user_id = i.user_id AND order0_.order_id = i.order_id"
            + " WHERE order0_.user_id IN (%s, %s) AND order0_.order_id BETWEEN %s AND %s ORDER BY i.item_id DESC) row_ WHERE rownum <= ?)";
    
    private static final String SELECT_PAGING_WITH_OFFSET_SQL = "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT"
            + " order0_.order_id as order_id, order0_.status as status, order0_.user_id as user_id"
            + " FROM t_order order0_ JOIN t_order_item i ON order0_.user_id = i.user_id AND order0_.order_id = i.order_id"
            + " WHERE order0_.user_id IN (%s, %s) AND order0_.order_id BETWEEN %s AND %s ORDER BY i.item_id DESC) row_) WHERE rownum > ?";
    
    private static final String SELECT_GROUP_BY_USER_ID_SQL = "SELECT user_id AS usrid FROM t_order GROUP BY user_id";
    
    private static final String SELECT_USER_ID_BY_STATUS_SQL = "SELECT user_id AS usrid FROM t_order WHERE status = 'init'";
    
    private static final String SELECT_USER_ID_BY_IN_STATUS_SQL = "SELECT user_id AS usrid FROM t_order WHERE status IN (? ,? ,? ,? ,?)";
    
    private static final String SELECT_USER_ID_BY_STATUS_ORDER_BY_USER_ID_SQL = "SELECT user_id AS usrid FROM t_order WHERE status = 'init' ORDER BY user_id";
    
    private static final String SELECT_USER_ID_WHERE_ORDER_ID_IN_SQL = "SELECT user_id AS usrid FROM t_order WHERE order_id IN (%s, %s)";
    
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
    
    @Override
    public String getSelectUserIdByStatusSql() {
        return SELECT_USER_ID_BY_STATUS_SQL;
    }
    
    @Override
    public String getSelectUserIdByInStatusSql() {
        return SELECT_USER_ID_BY_IN_STATUS_SQL;
    }
    
    @Override
    public String getSelectUserIdByStatusOrderByUserIdSql() {
        return SELECT_USER_ID_BY_STATUS_ORDER_BY_USER_ID_SQL;
    }
    
    @Override
    public String getSelectUserIdWhereOrderIdInSql() {
        return SELECT_USER_ID_WHERE_ORDER_ID_IN_SQL;
    }
}
