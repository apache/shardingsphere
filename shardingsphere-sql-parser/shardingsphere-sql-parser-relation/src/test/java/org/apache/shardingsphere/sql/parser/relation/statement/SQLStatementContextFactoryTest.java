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

package org.apache.shardingsphere.sql.parser.relation.statement;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.relation.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLStatementContextFactoryTest {
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSelectStatement() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getGroupBy()).thenReturn(Optional.<GroupBySegment>absent());
        when(selectStatement.getOrderBy()).thenReturn(Optional.<OrderBySegment>absent());
        when(selectStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.of(new LimitSegment(0, 10, null, null)));
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        when(projectionsSegment.getProjections()).thenReturn(Collections.<ProjectionSegment>emptyList());
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(null, null, null, selectStatement);
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof SelectSQLStatementContext);
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.useDefaultColumns()).thenReturn(false);
        when(insertStatement.findSQLSegment(LimitSegment.class)).thenReturn(Optional.of(new LimitSegment(0, 10, null, null)));
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(null, null, null, insertStatement);
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof InsertSQLStatementContext);
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementNotInstanceOfSelectStatementAndInsertStatement() {
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(null, null, null, mock(SQLStatement.class));
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof CommonSQLStatementContext);
    }
}
