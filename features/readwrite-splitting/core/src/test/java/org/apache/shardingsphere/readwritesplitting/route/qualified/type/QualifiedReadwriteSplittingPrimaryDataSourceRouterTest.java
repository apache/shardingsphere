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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QualifiedReadwriteSplittingPrimaryDataSourceRouterTest {
    
    private ReadwriteSplittingDataSourceRule rule;
    
    private QualifiedReadwriteSplittingPrimaryDataSourceRouter router;
    
    @Mock
    private CommonSQLStatementContext<SQLStatement> sqlStatementContext;

    @BeforeEach
    void setUp() {
        router = new QualifiedReadwriteSplittingPrimaryDataSourceRouter();
    }

    @Test
    void assertWriteRouteStatement() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(new LockSegment(0, 1)));
        assertThat(router.isQualified(sqlStatementContext, null), is(true));
        MySQLUpdateStatement updateStatement = mock(MySQLUpdateStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(updateStatement);
        assertThat(router.isQualified(sqlStatementContext, null), is(true));
    }
    
    @Test
    void assertHintRouteWriteOnly() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(sqlStatementContext.isHintWriteRouteOnly()).thenReturn(false);
        assertThat(router.isQualified(sqlStatementContext, null), is(false));
        when(sqlStatementContext.isHintWriteRouteOnly()).thenReturn(true);
        assertThat(router.isQualified(sqlStatementContext, null), is(true));
    }
}
