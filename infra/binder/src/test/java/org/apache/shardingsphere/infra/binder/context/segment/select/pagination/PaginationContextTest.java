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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination;

import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaginationContextTest {
    
    @Test
    void assertSegmentWithNullOffsetSegment() {
        PaginationValueSegment rowCountSegment = getRowCountSegment();
        PaginationContext paginationContext = new PaginationContext(null, rowCountSegment, getParameters());
        assertTrue(paginationContext.isHasPagination());
        assertNull(paginationContext.getOffsetSegment().orElse(null));
        assertThat(paginationContext.getRowCountSegment().orElse(null), is(rowCountSegment));
    }
    
    @Test
    void assertGetSegmentWithRowCountSegment() {
        PaginationValueSegment offsetSegment = getOffsetSegment();
        PaginationContext paginationContext = new PaginationContext(offsetSegment, null, getParameters());
        assertTrue(paginationContext.isHasPagination());
        assertThat(paginationContext.getOffsetSegment().orElse(null), is(offsetSegment));
        assertNull(paginationContext.getRowCountSegment().orElse(null));
    }
    
    @Test
    void assertGetActualOffset() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    void assertGetActualOffsetWithNumberLiteralPaginationValueSegment() {
        assertThat(new PaginationContext(getOffsetSegmentWithNumberLiteralPaginationValueSegment(),
                getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualOffset(), is(30L));
    }
    
    @Test
    void assertGetActualOffsetWithNullOffsetSegment() {
        assertThat(new PaginationContext(null, getRowCountSegment(), getParameters()).getActualOffset(), is(0L));
    }
    
    @Test
    void assertGetActualRowCount() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getActualRowCount().orElse(null), is(20L));
    }
    
    @Test
    void assertGetActualRowCountWithNumberLiteralPaginationValueSegment() {
        assertThat(new PaginationContext(getOffsetSegmentWithNumberLiteralPaginationValueSegment(),
                getRowCountSegmentWithNumberLiteralPaginationValueSegment(), getParameters()).getActualRowCount().orElse(null), is(20L));
    }
    
    @Test
    void assertGetActualRowCountWithNullRowCountSegment() {
        assertNull(new PaginationContext(getOffsetSegment(), null, getParameters()).getActualRowCount().orElse(null));
    }
    
    private PaginationValueSegment getOffsetSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(28, 30, 30);
    }
    
    private PaginationValueSegment getRowCountSegmentWithNumberLiteralPaginationValueSegment() {
        return new NumberLiteralLimitValueSegment(32, 34, 20);
    }
    
    @Test
    void assertGetOffsetParameterIndex() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getOffsetParameterIndex().orElse(null), is(0));
    }
    
    @Test
    void assertGetRowCountParameterIndex() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRowCountParameterIndex().orElse(null), is(1));
    }
    
    private PaginationValueSegment getOffsetSegment() {
        return new ParameterMarkerLimitValueSegment(28, 30, 0);
    }
    
    private PaginationValueSegment getRowCountSegment() {
        return new ParameterMarkerLimitValueSegment(32, 34, 1);
    }
    
    private List<Object> getParameters() {
        return Arrays.asList(30, 20);
    }
    
    @Test
    void assertGetRevisedOffset() {
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedOffset(), is(0L));
    }
    
    @Test
    void assertGetRevisedRowCountForMySQL() {
        getRevisedRowCount(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountForOracle() {
        getRevisedRowCount(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountForPostgreSQL() {
        getRevisedRowCount(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountForSQL92() {
        getRevisedRowCount(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountForSQLServer() {
        getRevisedRowCount(new SQLServerGenericSelectStatement());
    }
    
    private void getRevisedRowCount(final GenericSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(selectStatementContext), is(50L));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertGetRevisedRowCountWithMaxForMySQL() {
        getRevisedRowCountWithMax(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountWithMaxForOracle() {
        getRevisedRowCountWithMax(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountWithMaxForPostgreSQL() {
        getRevisedRowCountWithMax(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountWithMaxForSQL92() {
        getRevisedRowCountWithMax(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertGetRevisedRowCountWithMaxForSQLServer() {
        getRevisedRowCountWithMax(new SQLServerGenericSelectStatement());
    }
    
    private void getRevisedRowCountWithMax(final GenericSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.LAST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.DESC, NullsOrderType.LAST))));
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockDatabase());
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
        assertThat(new PaginationContext(getOffsetSegment(), getRowCountSegment(), getParameters()).getRevisedRowCount(selectStatementContext), is((long) Integer.MAX_VALUE));
    }
}
