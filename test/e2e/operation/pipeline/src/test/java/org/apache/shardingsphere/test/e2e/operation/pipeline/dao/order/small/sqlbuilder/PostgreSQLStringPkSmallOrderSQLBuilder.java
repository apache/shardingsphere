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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.small.sqlbuilder;

public final class PostgreSQLStringPkSmallOrderSQLBuilder implements StringPkSmallOrderSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String tableName) {
        return String.format("""
                CREATE TABLE %s (
                order_id varchar(255) NOT NULL,
                user_id int NOT NULL,
                status varchar(255) NULL,
                t_unsigned_int int NULL,
                PRIMARY KEY (order_id)
                )
                """, tableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
