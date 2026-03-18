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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

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
                order_id, user_id, status,
                c_int, c_smallint,
                c_float, c_double, c_numeric, c_boolean,
                c_char, c_text, c_bytea, c_raw,
                c_date, c_time, c_smalldatetime, c_timestamp, c_timestamptz, c_interval,
                c_array, c_json, c_jsonb,
                c_uuid, c_hash32,
                c_tsvector, c_tsquery, c_bit,
                c_int4range, c_daterange, c_tsrange,
                c_reltime, c_abstime,
                c_point, c_lseg, c_box, c_circle, c_bitvarying,
                c_cidr, c_inet, c_macaddr,
                c_hll, c_money
                ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, qualifiedTableName);
    }
    
    @Override
    public List<Object[]> generateInsertData(final KeyGenerateAlgorithm keyGenerateAlgorithm, final int recordCount) {
        List<Object[]> result = new ArrayList<>(recordCount);
        for (int i = 0; i < recordCount; i++) {
            Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            byte[] bytesValue = {Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE};
            Object[] params = new Object[]{
                    orderId, PipelineCaseHelper.generateInt(0, 1000), "'status'" + i,
                    PipelineCaseHelper.generateInt(-1000, 9999), PipelineCaseHelper.generateInt(0, 100),
                    PipelineCaseHelper.generateFloat(), PipelineCaseHelper.generateDouble(), BigDecimal.valueOf(PipelineCaseHelper.generateDouble()), false,
                    PipelineCaseHelper.generateString(6), "texts", bytesValue, bytesValue,
                    LocalDate.now(), LocalTime.now(), "2001-10-01", Timestamp.valueOf(LocalDateTime.now()), OffsetDateTime.now(), "0 years 0 mons 1 days 2 hours 3 mins 4 secs", "{1, 2, 3}",
                    PipelineCaseHelper.generateJsonString(8, false), PipelineCaseHelper.generateJsonString(8, true),
                    UUID.randomUUID().toString(), DigestUtils.md5Hex(orderId.toString()),
                    "'rat' 'sat'", "tsquery", "0000",
                    "[1,1000)", "[2020-01-02,2021-01-01)", "[2020-01-01 00:00:00,2021-01-01 00:00:00)",
                    "1 years 1 mons 10 days -06:00:00", "2000-01-02 00:00:00+00",
                    "(1.0,1.0)", "[(0.0,0.0),(2.0,2.0)]", "(3.0,3.0),(1.0,1.0)", "<(5.0,5.0),5.0>", "1111",
                    "192.168.0.0/16", "192.168.1.1", "08:00:2b:01:02:03",
                    "\\x484c4c00000000002b05000000000000000000000000000000000000", 999
            };
            result.add(params);
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
