/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.orderitem.sqlbuilder;

public final class PostgreSQLIntPkOrderItemSQLBuilder implements IntPkOrderItemSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String schemaPrefix) {
        return String.format("""
                CREATE TABLE %st_order_item (
                item_id int8 NOT NULL,
                order_id int8 NOT NULL,
                user_id int4 NOT NULL,
                status varchar(50),
                PRIMARY KEY (item_id)
                )
                """, schemaPrefix);
    }
    
    @Override
    public String buildPreparedInsertSQL(final String schemaPrefix) {
        return String.format("INSERT INTO %st_order_item (item_id, order_id, user_id, status) VALUES (?, ?, ?, ?)", schemaPrefix);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
