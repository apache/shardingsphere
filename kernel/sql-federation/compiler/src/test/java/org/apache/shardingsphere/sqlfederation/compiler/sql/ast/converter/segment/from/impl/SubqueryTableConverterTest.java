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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.DistinctConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.ProjectionsConverter;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DistinctConverter.class, ProjectionsConverter.class})
class SubqueryTableConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(SubqueryTableConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertBuildsExplicitTableWhenProjectionsAbsent() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_from"))));
        SubqueryTableSegment segment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, "sub"));
        segment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        SqlBasicCall actual = (SqlBasicCall) SubqueryTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.AS));
        assertThat(actual.getOperandList().get(0), instanceOf(SqlBasicCall.class));
        SqlBasicCall explicitTable = (SqlBasicCall) actual.getOperandList().get(0);
        assertThat(explicitTable.getOperator(), is(SqlStdOperatorTable.EXPLICIT_TABLE));
        assertThat(((SqlIdentifier) actual.getOperandList().get(1)).names, is(Collections.singletonList("alias")));
    }
    
    @Test
    void assertConvertDelegatesSelectConversionWhenProjectionsPresent() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projections);
        when(DistinctConverter.convert(projections)).thenReturn(Optional.empty());
        when(ProjectionsConverter.convert(projections)).thenReturn(Optional.of(new SqlNodeList(Collections.singletonList(mock(SqlNode.class)), SqlParserPos.ZERO)));
        SubqueryTableSegment segment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, selectStatement, "sub"));
        SqlBasicCall actual = (SqlBasicCall) SubqueryTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.AS));
        assertThat(actual.getOperandList().get(0), instanceOf(SqlSelect.class));
        assertThat(actual.getOperandList().size(), is(1));
    }
}
