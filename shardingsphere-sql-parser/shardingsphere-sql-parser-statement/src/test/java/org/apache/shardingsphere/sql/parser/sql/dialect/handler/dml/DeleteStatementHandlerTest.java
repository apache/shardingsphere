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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DeleteStatementHandlerTest {
    
    @Test
    public void assertGetOrderBySegmentWithOrderBySegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        deleteStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        Optional<OrderBySegment> orderBySegment = DeleteStatementHandler.getOrderBySegment(deleteStatement);
        assertTrue(orderBySegment.isPresent());
    }
    
    @Test
    public void assertGetOrderBySegmentWithoutOrderBySegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        Optional<OrderBySegment> orderBySegment = DeleteStatementHandler.getOrderBySegment(deleteStatement);
        assertFalse(orderBySegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        deleteStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = DeleteStatementHandler.getLimitSegment(deleteStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        Optional<LimitSegment> limitSegment = DeleteStatementHandler.getLimitSegment(deleteStatement);
        assertFalse(limitSegment.isPresent());
    }
}
