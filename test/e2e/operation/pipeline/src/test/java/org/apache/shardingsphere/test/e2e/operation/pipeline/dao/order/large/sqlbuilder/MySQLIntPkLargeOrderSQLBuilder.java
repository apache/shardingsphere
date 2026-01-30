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

import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public final class MySQLIntPkLargeOrderSQLBuilder implements IntPkLargeOrderSQLBuilder {
    
    @Override
    public String buildCreateTableSQL(final String qualifiedTableName) {
        return String.format("""
                CREATE TABLE `%s` (
                `order_id` bigint NOT NULL,
                `user_id` int NOT NULL,
                `status` varchar ( 255 ) NULL,
                `t_mediumint` mediumint NULL,
                `t_smallint` smallint NULL,
                `t_tinyint` tinyint ( 3 ) NULL,
                `t_unsigned_int` int UNSIGNED NULL,
                `t_unsigned_mediumint` mediumint UNSIGNED NULL,
                `t_unsigned_smallint` smallint UNSIGNED NULL,
                `t_unsigned_tinyint` tinyint UNSIGNED NULL,
                `t_float` float NULL,
                `t_double` double NULL,
                `t_decimal` decimal ( 10, 2 ) NULL,
                `t_timestamp` timestamp(3) NULL,
                `t_datetime` datetime(6) NULL,
                `t_date` date NULL,
                `t_time` time(1) NULL,
                `t_year` year NULL,
                `t_bit` bit(32) NULL,
                `t_binary` binary(128) NULL,
                `t_varbinary` varbinary(255) NULL,
                `t_blob` blob NULL,
                `t_mediumblob` mediumblob NULL,
                `t_char` char ( 128 ) NULL,
                `t_text` text NULL,
                `t_mediumtext` mediumtext NULL,
                `t_enum` enum ('1', '2', '3') NULL,
                `t_set` set ('1', '2', '3') NULL,
                `t_json` json NULL COMMENT 'json test',
                PRIMARY KEY ( `order_id` ),
                KEY `idx_user_id` (`user_id`),
                KEY `idx_mulit` (`t_mediumint`,`t_unsigned_mediumint`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
                """, qualifiedTableName);
    }
    
    @Override
    public String buildPreparedInsertSQL(final String qualifiedTableName) {
        return String.format("""
                INSERT INTO %s (
                order_id, user_id, status,
                t_mediumint, t_smallint, t_tinyint, t_unsigned_int, t_unsigned_mediumint, t_unsigned_smallint, t_unsigned_tinyint,
                t_float, t_double, t_decimal,
                t_timestamp, t_datetime, t_date, t_time, t_year,
                t_bit, t_binary, t_varbinary, t_blob, t_mediumblob,
                t_char, t_text, t_mediumtext,
                t_enum, t_set,
                t_json
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, qualifiedTableName);
    }
    
    @Override
    public List<Object[]> generateInsertData(final KeyGenerateAlgorithm keyGenerateAlgorithm, final int recordCount) {
        List<Object[]> result = new ArrayList<>(recordCount);
        for (int i = 0; i < recordCount; i++) {
            int randomInt = PipelineCaseHelper.generateInt(-100, 100);
            Object orderId = keyGenerateAlgorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            int randomUnsignedInt = PipelineCaseHelper.generateInt(0, 100);
            LocalDateTime now = LocalDateTime.now();
            Object[] params = {
                    orderId, PipelineCaseHelper.generateInt(0, 100), PipelineCaseHelper.generateString(6),
                    randomInt, randomInt, randomInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt, randomUnsignedInt,
                    PipelineCaseHelper.generateFloat(), PipelineCaseHelper.generateDouble(), BigDecimal.valueOf(PipelineCaseHelper.generateDouble()),
                    now, now, now.toLocalDate(), now.toLocalTime(), Year.now().getValue(),
                    "1", "t", "e", "s", "t",
                    PipelineCaseHelper.generateString(2), "☠️x☺️x✋x☹️", PipelineCaseHelper.generateString(1),
                    "1", "2",
                    PipelineCaseHelper.generateJsonString(32, false)
            };
            result.add(params);
        }
        return result;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
