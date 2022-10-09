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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;
import org.apache.shardingsphere.traffic.factory.TrafficAlgorithmFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SQLRegexTrafficAlgorithmTest {
    
    private SQLRegexTrafficAlgorithm sqlRegexAlgorithm;
    
    @Before
    public void setUp() {
        sqlRegexAlgorithm = (SQLRegexTrafficAlgorithm) TrafficAlgorithmFactory.newInstance(new AlgorithmConfiguration("SQL_REGEX", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("regex", "(?i)^(UPDATE|SELECT).*WHERE user_id.*");
        return result;
    }
    
    @Test
    public void assertMatchWhenExistSQLRegexMatch() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE t_order SET order_id = ? WHERE user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "update `t_order`  SET `order_id` = ? WHERE user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from `t_order` where user_id = ?;")));
        assertTrue(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE `t_order_item` SET `order_id` = ? WHERE user_id = ?;")));
    }
    
    @Test
    public void assertMatchWhenNotExistSQLRegexMatch() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "SELECT * FROM t_order")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from  t_order;")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "select *  from `t_order`;")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "TRUNCATE TABLE `t_order` ")));
        assertFalse(sqlRegexAlgorithm.match(new SegmentTrafficValue(sqlStatement, "UPDATE `t_order` SET `order_id` = ?;")));
    }
}
