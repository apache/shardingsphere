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

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertWithLimitAndAlias() {
        UpdateStatement updateStatement = createUpdateStatement(true);
        LimitSegment limit = new LimitSegment(0, 0, new NumberLiteralLimitValueSegment(0, 0, 1L), new ParameterMarkerLimitValueSegment(0, 0, 0));
        updateStatement.setLimit(limit);
        SqlOrderBy actual = (SqlOrderBy) new UpdateStatementConverter().convert(updateStatement);
        assertThat(actual.offset, instanceOf(SqlNode.class));
        assertThat(actual.fetch, instanceOf(SqlDynamicParam.class));
        SqlUpdate sqlUpdate = (SqlUpdate) actual.query;
        assertThat(sqlUpdate.getAlias(), instanceOf(SqlIdentifier.class));
    }
    
    @Test
    void assertConvertWithoutLimitButWithOrderBy() {
        UpdateStatement updateStatement = createUpdateStatement(false);
        updateStatement.setOrderBy(createOrderBySegment());
        SqlOrderBy actual = (SqlOrderBy) new UpdateStatementConverter().convert(updateStatement);
        assertNull(actual.offset);
        SqlUpdate sqlUpdate = (SqlUpdate) actual.query;
        assertNull(sqlUpdate.getAlias());
    }
    
    @Test
    void assertConvertWithLimitWithoutOffsetAndRowCount() {
        UpdateStatement updateStatement = createUpdateStatement(true);
        LimitSegment limit = new LimitSegment(0, 0, null, null);
        updateStatement.setLimit(limit);
        SqlOrderBy actual = (SqlOrderBy) new UpdateStatementConverter().convert(updateStatement);
        assertNull(actual.offset);
        assertNull(actual.fetch);
    }
    
    private UpdateStatement createUpdateStatement(final boolean withAlias) {
        UpdateStatement result = new UpdateStatement(databaseType);
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_update")));
        if (withAlias) {
            tableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("u")));
        }
        result.setTable(tableSegment);
        SetAssignmentSegment setAssignment = createSetAssignmentSegment();
        result.setSetAssignment(setAssignment);
        result.setWhere(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)));
        return result;
    }
    
    private SetAssignmentSegment createSetAssignmentSegment() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment),
                new ParameterMarkerExpressionSegment(0, 0, 0));
        return new SetAssignmentSegment(0, 0, Collections.singleton(columnAssignmentSegment));
    }
    
    private OrderBySegment createOrderBySegment() {
        ColumnOrderByItemSegment orderByItemSegment = new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("col")), OrderDirection.ASC, NullsOrderType.FIRST);
        return new OrderBySegment(0, 0, Collections.singleton(orderByItemSegment));
    }
}
