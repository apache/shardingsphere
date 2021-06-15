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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class UpdateStatementHandlerTest {
    
    @Test
    public void assertGetOrderBySegmentWithOrderBySegmentForMySQL() {
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        updateStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        Optional<OrderBySegment> orderBySegment = UpdateStatementHandler.getOrderBySegment(updateStatement);
        assertTrue(orderBySegment.isPresent());
    }
    
    @Test
    public void assertGetOrderBySegmentWithoutOrderBySegmentForMySQL() {
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        Optional<OrderBySegment> orderBySegment = UpdateStatementHandler.getOrderBySegment(updateStatement);
        assertFalse(orderBySegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForMySQL() {
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        updateStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = UpdateStatementHandler.getLimitSegment(updateStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForMySQL() {
        MySQLUpdateStatement updateStatement = new MySQLUpdateStatement();
        Optional<LimitSegment> limitSegment = UpdateStatementHandler.getLimitSegment(updateStatement);
        assertFalse(limitSegment.isPresent());
    }
}
