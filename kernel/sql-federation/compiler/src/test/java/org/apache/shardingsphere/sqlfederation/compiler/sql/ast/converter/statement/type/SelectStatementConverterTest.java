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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SelectStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertWithCombineAndLimit() {
        SelectStatement left = createBaseSelect(true, true);
        SelectStatement right = createBaseSelect(false, false);
        CombineSegment combineSegment = new CombineSegment(0, 0, new SubquerySegment(0, 0, left, "left"), CombineType.UNION, new SubquerySegment(0, 0, right, "right"));
        SelectStatement selectStatement = createBaseSelect(true, true);
        selectStatement.setCombine(combineSegment);
        LimitSegment limit = new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), new ParameterMarkerLimitValueSegment(0, 0, 0));
        selectStatement.setLimit(limit);
        SqlOrderBy actual = (SqlOrderBy) new SelectStatementConverter().convert(selectStatement);
        assertThat(actual.offset, instanceOf(SqlNode.class));
        assertThat(actual.fetch, instanceOf(SqlNode.class));
        assertThat(actual.query, instanceOf(SqlBasicCall.class));
    }
    
    @Test
    void assertConvertWithoutLimitButWithOrderByAndWindow() {
        SelectStatement selectStatement = createBaseSelect(false, false);
        selectStatement.setOrderBy(createOrderBySegment());
        selectStatement.setWindow(createWindowSegment());
        SqlOrderBy actual = (SqlOrderBy) new SelectStatementConverter().convert(selectStatement);
        assertNull(actual.offset);
        assertThat(((SqlSelect) actual.query).getWindowList(), isA(SqlNode.class));
    }
    
    private SelectStatement createBaseSelect(final boolean withWithSegment, final boolean distinct) {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections(createProjectionsSegment());
        result.getProjections().setDistinctRow(distinct);
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_select")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("t")));
        result.setFrom(fromTable);
        result.setWhere(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)));
        if (withWithSegment) {
            result.setWith(createWithSegment());
        }
        return result;
    }
    
    private WithSegment createWithSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(createProjectionsSegment());
        return new WithSegment(0, 0, Collections.emptyList(), false);
    }
    
    private WindowSegment createWindowSegment() {
        WindowItemSegment windowItemSegment = new WindowItemSegment(0, 0);
        windowItemSegment.setWindowName(new IdentifierValue("win"));
        windowItemSegment.setPartitionListSegments(Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0)));
        windowItemSegment.setOrderBySegment(createOrderBySegment());
        WindowSegment result = new WindowSegment(0, 0);
        result.getItemSegments().add(windowItemSegment);
        return result;
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        return result;
    }
    
    private OrderBySegment createOrderBySegment() {
        ColumnOrderByItemSegment orderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("col")), OrderDirection.ASC, NullsOrderType.FIRST);
        return new OrderBySegment(0, 0, Collections.singleton(orderByItemSegment));
    }
}
