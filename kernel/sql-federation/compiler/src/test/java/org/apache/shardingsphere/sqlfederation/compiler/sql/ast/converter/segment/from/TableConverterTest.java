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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl.DeleteMultiTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl.JoinTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl.SimpleTableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl.SubqueryTableConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SimpleTableConverter.class, JoinTableConverter.class, SubqueryTableConverter.class, DeleteMultiTableConverter.class})
class TableConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertDelegatesAllSupportedSegments() {
        SqlNode expectedSimpleNode = mock(SqlNode.class);
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_simple")));
        when(SimpleTableConverter.convert(simpleTableSegment)).thenReturn(Optional.of(expectedSimpleNode));
        SqlNode expectedJoinNode = mock(SqlNode.class);
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(simpleTableSegment);
        joinTableSegment.setRight(simpleTableSegment);
        when(JoinTableConverter.convert(joinTableSegment)).thenReturn(Optional.of(expectedJoinNode));
        SqlNode expectedSubqueryNode = mock(SqlNode.class);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, new SelectStatement(databaseType), "sub");
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, subquerySegment);
        when(SubqueryTableConverter.convert(subqueryTableSegment)).thenReturn(Optional.of(expectedSubqueryNode));
        SqlNode expectedDeleteNode = mock(SqlNode.class);
        DeleteMultiTableSegment deleteMultiTableSegment = new DeleteMultiTableSegment();
        deleteMultiTableSegment.setRelationTable(simpleTableSegment);
        when(DeleteMultiTableConverter.convert(deleteMultiTableSegment)).thenReturn(Optional.of(expectedDeleteNode));
        assertThat(TableConverter.convert(simpleTableSegment).orElse(null), is(expectedSimpleNode));
        assertThat(TableConverter.convert(joinTableSegment).orElse(null), is(expectedJoinNode));
        assertThat(TableConverter.convert(subqueryTableSegment).orElse(null), is(expectedSubqueryNode));
        assertThat(TableConverter.convert(deleteMultiTableSegment).orElse(null), is(expectedDeleteNode));
    }
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(TableConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertThrowsUnsupportedForUnknownSegment() {
        TableSegment unknownSegment = mock(TableSegment.class);
        UnsupportedSQLOperationException actualException = assertThrows(UnsupportedSQLOperationException.class, () -> TableConverter.convert(unknownSegment));
        assertThat(actualException.getMessage(), is("Unsupported SQL operation: Unsupported segment type: " + unknownSegment.getClass() + "."));
    }
}
