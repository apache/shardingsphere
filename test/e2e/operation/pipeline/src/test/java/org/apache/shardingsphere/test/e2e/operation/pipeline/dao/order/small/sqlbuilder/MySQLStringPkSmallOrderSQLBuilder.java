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

public final class MySQLStringPkSmallOrderSQLBuilder implements StringPkSmallOrderSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String tableName) {
        // TODO Delete t_unsigned_int (from primary_key/text_primary_key/mysql.xml)
        return String.format("""
                CREATE TABLE `%s` (
                `order_id` varchar(255) NOT NULL COMMENT 'pk id',
                `user_id` INT NOT NULL,
                `status` varchar(255) NULL,
                `t_unsigned_int` int UNSIGNED NULL,
                PRIMARY KEY ( `order_id` ),
                INDEX ( `user_id` )
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                """, tableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
