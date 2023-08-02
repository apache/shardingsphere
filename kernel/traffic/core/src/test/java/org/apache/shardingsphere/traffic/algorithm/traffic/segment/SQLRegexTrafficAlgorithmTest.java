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

package org.apache.shardingsphere.traffic.algorithm.traffic.segment;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SQLRegexTrafficAlgorithmTest {
    
    private SQLRegexTrafficAlgorithm sqlRegexAlgorithm;
    
    @BeforeEach
    void setUp() {
        sqlRegexAlgorithm = (SQLRegexTrafficAlgorithm) TypedSPILoader.getService(
                TrafficAlgorithm.class, "SQL_REGEX", PropertiesBuilder.build(new Property("regex", "(?i)^(UPDATE|SELECT).*WHERE user_id.*")));
    }
    
    @Test
    void assertMatchWhenExistSQLRegexMatch() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE t_order SET order_id = ? WHERE user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "update `t_order`  SET `order_id` = ? WHERE user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from `t_order` where user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE `t_order_item` SET `order_id` = ? WHERE user_id = ?;")));
    }
    
    @Test
    void assertMatchWhenNotExistSQLRegexMatch() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "SELECT * FROM t_order")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from  t_order;")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from `t_order`;")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "TRUNCATE TABLE `t_order` ")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE `t_order` SET `order_id` = ?;")));
    }
    
    @Test
    void assertInitWithIllegalProps() {
        assertThrows(IllegalArgumentException.class, () -> TypedSPILoader.getService(SQLRegexTrafficAlgorithm.class, "SQL_REGEX",
                PropertiesBuilder.build(new Property("regex", ""))));
    }
}
