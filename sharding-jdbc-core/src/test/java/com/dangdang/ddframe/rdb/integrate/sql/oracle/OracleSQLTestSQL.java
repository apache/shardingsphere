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
    
    private static final String SELECT_LIMIT_WITH_BINDING_TABLE_SQL = "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT" 
            + " order0_.order_id as order_id, order0_.status as status, order0_.user_id as user_id" 
            + " FROM t_order order0_ JOIN t_order_item i ON order0_.user_id = i.user_id AND order0_.order_id = i.order_id"
            + " WHERE order0_.user_id IN (%s, %s) AND order0_.order_id BETWEEN %s AND %s ORDER BY i.item_id DESC) row_ WHERE rownum <= ?) WHERE rownum > ?";
    
    @Override
    public String getSelectLimitWithBindingTableSql() {
        return SELECT_LIMIT_WITH_BINDING_TABLE_SQL;
    }
}
