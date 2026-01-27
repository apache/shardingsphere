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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.large.sqlbuilder;

public final class PostgreSQLIntPkLargeOrderSQLBuilder implements IntPkLargeOrderSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String tableName) {
        return String.format("""
                CREATE TABLE test.%s (
                order_id int8 NOT NULL,
                user_id int4 NOT NULL,
                status varchar ( 50 ) NULL,
                t_int2 int2 NULL,
                t_numeric numeric(10,2) NULL,
                t_bool boolean NULL,
                t_bytea bytea NULL,
                t_char char(10) NULL,
                t_varchar varchar(128) NULL,
                t_float float4 NULL,
                t_double float8 NULL,
                t_json json NULL,
                t_jsonb jsonb NULL,
                t_text TEXT NULL,
                t_date date NULL,
                t_time TIME NULL,
                t_timestamp timestamp NULL,
                t_timestamptz timestamptz NULL,
                PRIMARY KEY ( order_id )
                )
                """, tableName);
    }
    
    @Override
    public String buildPreparedInsertSQL(final String tableName) {
        return String.format("""
                INSERT INTO test.%s
                (order_id, user_id, status, t_int2, t_numeric, t_bool, t_bytea, t_char, t_varchar,
                t_float, t_double, t_json, t_jsonb, t_text, t_date, t_time, t_timestamp, t_timestamptz)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, tableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
