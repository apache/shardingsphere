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

public final class OpenGaussIntPkLargeOrderSQLBuilder implements IntPkLargeOrderSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String qualifiedTableName) {
        return String.format("""
                create table %s (
                order_id bigint,
                user_id integer,
                status character varying(50),
                c_int integer,
                c_smallint smallint,
                c_float real,
                c_double double precision,
                c_numeric numeric(10,2),
                c_boolean boolean,
                c_char character(32),
                c_text text,
                c_bytea bytea,
                c_raw bytea,
                c_date date,
                c_time time without time zone,
                c_smalldatetime smalldatetime,
                c_timestamp timestamp without time zone,
                c_timestamptz timestamp with time zone,
                c_interval interval,
                c_array integer[],
                c_json json,
                c_jsonb jsonb,
                c_uuid uuid,
                c_hash32 hash32,
                c_tsvector tsvector,
                c_tsquery tsquery,
                c_bit bit(4),
                c_int4range int4range,
                c_daterange daterange,
                c_tsrange tsrange,
                c_reltime reltime,
                c_abstime abstime,
                c_point point,
                c_lseg lseg,
                c_box box,
                c_circle circle,
                c_bitvarying bit varying(32),
                c_cidr cidr,
                c_inet inet,
                c_macaddr macaddr,
                c_hll hll(14,10,12,0),
                c_money money,
                PRIMARY KEY ( order_id )
                )
                """, qualifiedTableName);
    }
    
    @Override
    public String buildPreparedInsertSQL(final String qualifiedTableName) {
        return String.format("""
                INSERT INTO %s (
                order_id, user_id, status, c_int, c_smallint, c_float, c_double, c_numeric, c_boolean, c_char, c_text, c_bytea, c_raw, c_date, c_time,
                c_smalldatetime, c_timestamp, c_timestamptz, c_interval, c_array, c_json, c_jsonb, c_uuid, c_hash32, c_tsvector, c_tsquery, c_bit,
                c_int4range, c_daterange, c_tsrange, c_reltime, c_abstime, c_point, c_lseg, c_box, c_circle, c_bitvarying, c_cidr, c_inet, c_macaddr, c_hll, c_money
                ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, qualifiedTableName);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
