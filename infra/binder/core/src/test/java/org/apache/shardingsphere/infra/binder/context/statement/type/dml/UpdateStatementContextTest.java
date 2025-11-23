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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Test
    void assertNewInstance() {
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("tbl_2"));
        ownerSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(columnSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, columnSegment, null, null, null);
        when(whereSegment.getExpr()).thenReturn(expression);
        TableNameSegment tableNameSegment1 = new TableNameSegment(0, 0, new IdentifierValue("tbl_1"));
        tableNameSegment1.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        TableNameSegment tableNameSegment2 = new TableNameSegment(0, 0, new IdentifierValue("tbl_2"));
        tableNameSegment2.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        UpdateStatement updateStatement = createUpdateStatement(tableNameSegment1, tableNameSegment2);
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("tbl_1", "tbl_2"))));
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(whereSegment)));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_2", "tbl_2")));
    }
    
    private UpdateStatement createUpdateStatement(final TableNameSegment tableNameSegment1, final TableNameSegment tableNameSegment2) {
        SimpleTableSegment table1 = new SimpleTableSegment(tableNameSegment1);
        SimpleTableSegment table2 = new SimpleTableSegment(tableNameSegment2);
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(table1);
        joinTableSegment.setRight(table2);
        UpdateStatement result = new UpdateStatement(databaseType);
        result.setWhere(whereSegment);
        result.setTable(joinTableSegment);
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        return result;
    }
}
