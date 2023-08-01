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

package org.apache.shardingsphere.readwritesplitting.route.qualified.type;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualifiedReadwriteSplittingPrimaryDataSourceRouterTest {
    
    @Mock
    private CommonSQLStatementContext sqlStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Test
    void assertWriteRouteStatement() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getLock()).thenReturn(Optional.of(new LockSegment(0, 1)));
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(MySQLUpdateStatement.class));
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
    
    @Test
    void assertHintRouteWriteOnly() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(hintValueContext.isWriteRouteOnly()).thenReturn(false);
        assertFalse(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
        when(hintValueContext.isWriteRouteOnly()).thenReturn(true);
        assertTrue(new QualifiedReadwriteSplittingPrimaryDataSourceRouter().isQualified(sqlStatementContext, null, hintValueContext));
    }
}
