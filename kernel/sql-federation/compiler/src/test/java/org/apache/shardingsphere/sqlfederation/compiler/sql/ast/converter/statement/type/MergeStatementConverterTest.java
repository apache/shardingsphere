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

import org.apache.calcite.sql.SqlMerge;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MergeStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertWithUpdate() {
        MergeStatement mergeStatement = createMergeStatement(createSimpleTableSegment("target_table"), createSimpleTableSegment("source_table"), createUpdateStatement());
        SqlMerge actual = (SqlMerge) new MergeStatementConverter().convert(mergeStatement);
        assertThat(actual.getCondition(), isA(SqlNode.class));
        assertThat(actual.getUpdateCall(), isA(SqlUpdate.class));
    }
    
    @Test
    void assertConvertWithoutUpdate() {
        MergeStatement mergeStatement = createMergeStatement(createSimpleTableSegment("target_table"), createSimpleTableSegment("source_table"));
        SqlMerge actual = (SqlMerge) new MergeStatementConverter().convert(mergeStatement);
        assertNull(actual.getUpdateCall());
    }
    
    @Test
    void assertConvertUpdateWithEmptyTable() {
        UpdateStatement updateStatement = createUpdateStatement(createSimpleTableSegment("DUAL"), false);
        MergeStatement mergeStatement = createMergeStatement(createSimpleTableSegment("target_table"), createSimpleTableSegment("source_table"), updateStatement);
        SqlMerge actual = (SqlMerge) new MergeStatementConverter().convert(mergeStatement);
        assertNotNull(actual.getUpdateCall());
        assertThat(actual.getUpdateCall().getTargetTable(), is(SqlNodeList.EMPTY));
    }
    
    private MergeStatement createMergeStatement(final SimpleTableSegment target, final SimpleTableSegment source) {
        return createMergeStatement(target, source, null);
    }
    
    private MergeStatement createMergeStatement(final SimpleTableSegment target, final SimpleTableSegment source, final UpdateStatement updateStatement) {
        return MergeStatement.builder().databaseType(databaseType).target(target).source(source)
                .expression(new ExpressionWithParamsSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0))).update(updateStatement).build();
    }
    
    private UpdateStatement createUpdateStatement() {
        return createUpdateStatement(createSimpleTableSegment("t_update"), true);
    }
    
    private UpdateStatement createUpdateStatement(final SimpleTableSegment table, final boolean withAlias) {
        if (withAlias) {
            table.setAlias(new AliasSegment(0, 0, new IdentifierValue("u")));
        }
        SetAssignmentSegment setAssignment = createSetAssignmentSegment();
        return UpdateStatement.builder()
                .databaseType(databaseType)
                .table(table)
                .setAssignment(setAssignment)
                .where(new WhereSegment(0, 0, new ParameterMarkerExpressionSegment(0, 0, 0)))
                .build();
    }
    
    private SetAssignmentSegment createSetAssignmentSegment() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment),
                new ParameterMarkerExpressionSegment(0, 0, 0));
        return new SetAssignmentSegment(0, 0, Collections.singleton(columnAssignmentSegment));
    }
    
    private SimpleTableSegment createSimpleTableSegment(final String tableName) {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
    }
}
